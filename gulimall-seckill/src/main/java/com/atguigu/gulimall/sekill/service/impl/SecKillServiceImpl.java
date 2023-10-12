package com.atguigu.gulimall.sekill.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.sekill.feign.CouponFeignService;
import com.atguigu.gulimall.sekill.feign.ProductFeignService;
import com.atguigu.gulimall.sekill.interceptor.SeckillInterceptor;
import com.atguigu.gulimall.sekill.service.SecKillService;
import com.atguigu.gulimall.sekill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.sekill.vo.SeckillSessionWithSkusVo;
import com.atguigu.gulimall.sekill.vo.SeckillSkuVo;
import com.atguigu.gulimall.sekill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
    //V: sessionId+"-"+skuId的List
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    //K: 固定值SECKILL_CHARE_PREFIX
    //V: hash，k为sessionId+"-"+skuId，v为对应的商品信息SeckillSkuRedisTo
    private final String SECKILL_SKUS_PREFIX = "seckill:skus";

    //K: SKU_STOCK_SEMAPHORE+商品随机码
    //V: 秒杀的库存件数
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码





    @Override
    public void uploadSeckillSkuLatest3Days() {

        R r  = couponFeignService.getSecKillSession3Days();

        if (r.getCode() == 0)
        {
            List< SeckillSessionWithSkusVo > sessions =  r.getData("data", new TypeReference<List< SeckillSessionWithSkusVo >>(){});
            //在redis中分别保存秒杀场次信息和场次对应的秒杀商品信息

            saveSecKillSession(sessions);
            saveSecKillSku(sessions);
        }

    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSecKill() {

        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        Long currentTime = System.currentTimeMillis();
        for (String key : keys) {
            String[] startEndRange = key.replace(SESSION_CACHE_PREFIX, "").split("_");
            Long startTime = Long.parseLong(startEndRange[0]);
            Long endTime = Long.parseLong(startEndRange[1]);
            if ( startTime < currentTime && currentTime < endTime )
            {
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
                List<SeckillSkuRedisTo> collect = range.stream().map(s -> {
                    String json = (String) ops.get(s);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    return redisTo;
                }).collect(Collectors.toList());
                return collect;
            }

        }

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {

        BoundHashOperations<String, String, String> ops =  redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
        Set<String> keys = ops.keys();
        for (String key : keys) {
            if(Pattern.matches("\\d-"+ skuId, key))
            {
                String data =ops.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(data, SeckillSkuRedisTo.class);

                if(redisTo != null){
                    Long currentTime = System.currentTimeMillis();

                    //当前活动在有效期，暴露商品随机码返回
                    if ( currentTime < redisTo.getEndTime() && currentTime > redisTo.getStartTime())
                    {
                        return  redisTo;
                    }

                    redisTo.setRandomCode(null);
                    return redisTo;
                }
            }

        }

        return null ;
    }

    @Override
    public String secKll(String killId, String key, Integer num) {

        // 校验合法性
        //  1 检查时间的合法性
        // 2 检查随机码
        // 验证购物数量是否合理
        // 验证是否已经购买过， 米的能行， 只要秒杀成功就去占位  userid_sessionid_skuid
        //
        // SETNX 占位  setIfAbsent函数执行占位处理， 如果占位过返回真， 否则为否
        // 确认库存， 利用信号量查看能否获取库存
        // 发送快速下单的消息队列
        // 生成ID返回ID

        BoundHashOperations<String, String , String> hashOperations =  redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
        String s = hashOperations.get(killId);
        if (!StringUtils.isEmpty(s))
        {
            SeckillSkuRedisTo secKillSku = JSON.parseObject(s, SeckillSkuRedisTo.class);
            Long currentTime = System.currentTimeMillis();
            //  1 检查时间的合法性
            if( currentTime > secKillSku.getStartTime() && currentTime < secKillSku.getEndTime())
            {
                String redisKey = secKillSku.getPromotionSessionId()+"-"+ secKillSku.getSkuId();
                // 随机码
                if( secKillSku.getRandomCode().equals(key) && redisKey.equals(killId))
                {
                    // 购物适量是否合理
                    if (num <= secKillSku.getSeckillLimit())
                    {
                        MemberResponseVo member = SeckillInterceptor.loginUser.get();
                        String userSessSku =  member.getId() + "_"+ secKillSku.getPromotionSessionId() +"_" + secKillSku.getSkuId();
                        Long timeToLive = secKillSku.getEndTime() - currentTime;
                        // 占位是否有购买记录
                        Boolean occupy =  redisTemplate.opsForValue().setIfAbsent(userSessSku, num.toString(), timeToLive, TimeUnit.MILLISECONDS);
                        // 占位成功 没有购买记录
                        if(occupy)
                        {
                            String stockKey = SKU_STOCK_SEMAPHORE + secKillSku.getRandomCode();

                            RSemaphore  stockSemaphore =   redissonClient.getSemaphore(stockKey);
                            try {
                               boolean acquire = stockSemaphore.tryAcquire(num, 100, TimeUnit.SECONDS);
                               // 确认库存， 获取了库存
                               if(acquire)
                               {
                                   String orderID = IdWorker.getTimeId();
                                   SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                                   secKillOrderTo.setOrderSn(orderID);
                                   secKillOrderTo.setNum(num);
                                   secKillOrderTo.setMemberId(member.getId());
                                   secKillOrderTo.setPromotionSessionId(secKillSku.getPromotionSessionId());
                                   secKillOrderTo.setSeckillPrice(secKillSku.getSeckillPrice());
                                   secKillOrderTo.setSkuId(secKillSku.getSkuId());

                                   rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", secKillOrderTo);
                                   return orderID;
                               }

                            } catch (InterruptedException e) {
                                return null;
                            }

                        }
                    }
                }
            }

        }
        return null;
    }

    private void saveSecKillSku(List<SeckillSessionWithSkusVo> sessions) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_SKUS_PREFIX);
        sessions.stream().forEach(session->{
            session.getRelations().stream().forEach(sku->{
                String key = sku.getPromotionSessionId() +"-"+ sku.getSkuId();
                if (!ops.hasKey(key)){
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //1. 保存SeckillSkuVo信息
                    BeanUtils.copyProperties(sku,redisTo);
                    //2. 保存开始结束时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //3. 远程查询sku信息并保存
                    R r = productFeignService.info(sku.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(skuInfo);
                    }
                    //4. 生成商品随机码，防止恶意攻击
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);
                    //5. 序列化为json并保存
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(key,jsonString);
                    //5. 使用库存作为Redisson信号量限制库存
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(sku.getSeckillCount());
                }
            });
        });

        //

    }

    private void saveSecKillSession(List< SeckillSessionWithSkusVo > secKillSessions )
    {
        secKillSessions.stream().forEach(session->{
            String key = SESSION_CACHE_PREFIX + session.getStartTime().getTime() +"_" + session.getEndTime().getTime();
            //当前活动信息未保存过
            if (!redisTemplate.hasKey(key)){
                List<String> values = session.getRelations().stream()
                        .map(sku -> sku.getPromotionSessionId() +"-"+ sku.getSkuId())
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key,values);
            }

        });

    }

}
