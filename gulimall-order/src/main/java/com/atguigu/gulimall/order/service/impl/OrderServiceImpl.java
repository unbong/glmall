package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.OrderStatusEnum;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.OrderTo;
import com.atguigu.common.to.SecKillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.To.OrderCreateTo;
import com.atguigu.gulimall.order.To.SpuInfoTo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartServiceFeign;
import com.atguigu.gulimall.order.feign.MemberServiceFeign;
import com.atguigu.gulimall.order.feign.ProductServiceFeign;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.OrderIntercepter;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    MemberServiceFeign memberFeign;

    @Autowired
    CartServiceFeign cartFeign;

    @Autowired
    WareFeignService wareFeign;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductServiceFeign productServiceFeign;


    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getConfirmOrder() throws ExecutionException, InterruptedException {

        MemberResponseVo member =  OrderIntercepter.threadLocal.get();

        OrderConfirmVo orderConfirm = new OrderConfirmVo();
        RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();

        //1 获取会员地址列表
        CompletableFuture getMemberAddr =  CompletableFuture.runAsync(()->{

            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r = memberFeign.getAddressByUserId(member.getId());

            if(r.getCode() == 0)
            {
               List<MemberAddressVo> memberAddressVos = r.getData("memberReceiveAddress", new TypeReference<List<MemberAddressVo>>(){});
                orderConfirm.setMemberAddressVos(memberAddressVos);

            }

            },executor);



        // 2 获取购物项目
        CompletableFuture getOrderItem = CompletableFuture.runAsync(()->{

            RequestContextHolder.setRequestAttributes(requestAttributes);

            List<OrderItemVo> items =  cartFeign.getCheckItems();
            orderConfirm.setOrderItemVos(items);

        },executor).thenAcceptAsync(items->{
            List<Long> skuIds = orderConfirm.getOrderItemVos().stream().map(item-> item.getSkuId()).collect(Collectors.toList());
            R wareInfo =  wareFeign.getHasStock(skuIds);

            List<SkuHasStockVo> skockVos = wareInfo.getData("data", new TypeReference<List<SkuHasStockVo>>(){});

            Map<Long, Boolean> stockMap =   skockVos.stream().collect(Collectors.toMap(item->item.getSkuId(), item->item.getHasStock()));

            orderConfirm.setStocks(stockMap);
        },executor);



        // 3 积分

        orderConfirm.setIntegretion(member.getIntegration());

        //5. 总价自动计算
        //6. 防重令牌

        CompletableFuture.allOf(getMemberAddr, getOrderItem).get();

        String token = java.util.UUID.randomUUID().toString();
        orderConfirm.setOrderToken(token);

        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(), token, 30, TimeUnit.MINUTES);

        log.info("订单确认数据取得完场。 ",orderConfirm.toString());

        return orderConfirm;
    }


    //@GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {

        MemberResponseVo member =  OrderIntercepter.threadLocal.get();
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();

        //1. 验证防重令牌
        String token = submitVo.getOrderToken();
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long code = (Long) redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()), submitVo.getOrderToken());


        //1.1 防重令牌验证失败
        if( code == 0L)
        {
            responseVo.setCode(1);

        }
        else{
            //2. 创建订单、订单项
            OrderCreateTo order = createOrderTo(member, submitVo);

            //3. 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue() ) < 0.01){
                //4. 保存订单
                saveOrder(order);
                //5. 锁定库存
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    return orderItemVo;
                }).collect(Collectors.toList());
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                lockVo.setLocks(orderItemVos);
                R r = wareFeign.orderLockStock(lockVo);
                //5.1 锁定库存成功
                if (r.getCode()==0){
//
                    responseVo.setOrder(order.getOrder());
                    responseVo.setCode(0);

                    //发送消息到订单延迟队列，判断过期订单
                    //清除购物车记录
                    //int i = 10 / 0;

                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());



                    return responseVo;
                }else {
                    //5.1 锁定库存失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }

            }else {
                //验价失败
                responseVo.setCode(2);
                return responseVo;
            }

        }

        return responseVo;
    }

    @Override
    public OrderEntity infoByOrderSn(String orderSn) {

       return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

    }

    @Override
    public void closeOrder(OrderEntity order) {

        // 针对未支付的订单进行关闭
        OrderEntity orderEnt = this.getById(order.getId());

        if(orderEnt.getStatus() == OrderStatusEnum.CREATE_NEW.getCode())
        {
            OrderEntity update = new OrderEntity();
            update.setId(order.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);

            // 关单后也要对库存进行解锁，为了防止服务延迟照成的错误等问题
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEnt, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other",orderTo);
        }

    }

    @Override
    public void createSecKillOrder(SecKillOrderTo orderTo) {
        MemberResponseVo memberResponseVo = OrderIntercepter.threadLocal.get();
        //1. 创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setMemberId(orderTo.getMemberId());
        if (memberResponseVo!=null){
            orderEntity.setMemberUsername(memberResponseVo.getUsername());
        }
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setCreateTime(new Date());
        orderEntity.setPayAmount(orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum())));
        this.save(orderEntity);
        //2. 创建订单项
        R r = productServiceFeign.info(orderTo.getSkuId());
        if (r.getCode() == 0) {
            SeckillSkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SeckillSkuInfoVo>() {
            });
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderTo.getOrderSn());
            orderItemEntity.setSpuId(skuInfo.getSpuId());
            orderItemEntity.setCategoryId(skuInfo.getCatalogId());
            orderItemEntity.setSkuId(skuInfo.getSkuId());
            orderItemEntity.setSkuName(skuInfo.getSkuName());
            orderItemEntity.setSkuPic(skuInfo.getSkuDefaultImg());
            orderItemEntity.setSkuPrice(skuInfo.getPrice());
            orderItemEntity.setSkuQuantity(orderTo.getNum());
            orderItemService.save(orderItemEntity);
        }
    }

    private void saveOrder(OrderCreateTo orderTo) {
        OrderEntity order = orderTo.getOrder();
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        this.save(order);
        orderItemService.saveBatch(orderTo.getOrderItems());
    }

    private OrderCreateTo createOrderTo(MemberResponseVo member, OrderSubmitVo submitVo) {
        OrderCreateTo order = new OrderCreateTo();

        // 生成订单号
        String orderSn = IdWorker.getTimeId();

        // 构建订单

        OrderEntity orderEntity = buildOrder(member, submitVo, orderSn);

        //构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 计算价格
        compute(orderEntity, orderItemEntities);
        OrderCreateTo createTo = new OrderCreateTo();
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;

    }


    private void compute(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = BigDecimal.ZERO;
        //优惠价格
        BigDecimal promotion=new BigDecimal("0.0");
        BigDecimal integration=new BigDecimal("0.0");
        BigDecimal coupon=new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total=total.add(orderItemEntity.getRealAmount());
            promotion=promotion.add(orderItemEntity.getPromotionAmount());
            integration=integration.add(orderItemEntity.getIntegrationAmount());
            coupon=coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> checkedItems = cartFeign.getCheckItems();
        List<OrderItemEntity> orderItemEntities = checkedItems.stream().map((item) -> {
            OrderItemEntity orderItemEntity = buildOrderItem(item);
            //1) 设置订单号
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;

    }

    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        Long skuId = item.getSkuId();

        //设置SKU属性
         orderItemEntity.setSkuId(skuId);
         orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        //3) 通过skuId查询spu相关属性并设置
        R r = productServiceFeign.getSpuBySkuId(skuId);
        if(r.getCode()== 0)
        {
            SpuInfoTo spuInfoTo = r.getData("data", new TypeReference<SpuInfoTo>(){});
            orderItemEntity.setSpuId(spuInfoTo.getId());
            orderItemEntity.setSpuName(spuInfoTo.getSpuName());
            orderItemEntity.setSpuBrand(spuInfoTo.getBrandName());
            orderItemEntity.setCategoryId(spuInfoTo.getCatalogId());

        }

        //4) 商品的优惠信息(不做)

        //5) 商品的积分成长，为价格x数量
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());


        //6) 订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);


        //7) 实际价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }

    private OrderEntity buildOrder(MemberResponseVo member, OrderSubmitVo submitVo, String orderSn) {

        OrderEntity orderEntity =new OrderEntity();

        orderEntity.setOrderSn(orderSn);

        //2) 设置用户信息
        orderEntity.setMemberId(member.getId());
        orderEntity.setMemberUsername(member.getUsername());

        //3) 获取邮费和收件人信息并设置
        FareVo fareVo = wareFeign.getFare(submitVo.getAddrId());
        BigDecimal fare = fareVo.getFare();
        orderEntity.setFreightAmount(fare);
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());

        //4) 设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);

        return orderEntity;
    }

}