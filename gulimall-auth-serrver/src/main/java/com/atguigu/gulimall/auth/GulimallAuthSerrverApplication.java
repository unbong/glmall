package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
public class GulimallAuthSerrverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthSerrverApplication.class, args);
    }


    /**
     * 继承WebMvcConfigular
     * 实现addViewController
     *
     *  在thirdPaorty中添加短信验证码功能 使用阿里云
     *
     *  为第三方服务生成被调用验证码Controller
     *
     *  1 接口防刷 防止有人利用Ajax请求路径恶意使用
     *
     *  2 验证码再次校验 使用redis
     */
}
