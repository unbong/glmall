package com.atguigu.gulimall.sekill.schedule;


import com.atguigu.gulimall.sekill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
//@EnableScheduling
@Slf4j
public class SecKillSchedule {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SecKillService secKillService;

    //秒杀商品上架功能的锁
    private final String upload_lock = "seckill:upload:lock";

    //@Async
    //@Scheduled(cron ="0 0 3 * * ?")
    @Scheduled(cron ="0/30 * 14 * * ?")
    public void update3DaySecKill()
    {
        log.info("秒杀上架功能开启");
        // 追加可重入锁
        RLock lock= redissonClient.getLock(upload_lock);
        lock.lock();
        try{
            secKillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }

    }
}
