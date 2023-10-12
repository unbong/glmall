package com.atguigu.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyThreadExcutor {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool)
    {
        return new ThreadPoolExecutor(pool.getCoreSize(),pool.getMaxSize(), pool.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.AbortPolicy());
    }


}
