package com.atguitu.gulimall.gulimallsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 *  1 thyme leaf 依赖追加
 *  2 html文件添加thymeleaf模板
 *  3 nginx中添加静态文件
 *  4 修改html中静态资源的路径
 *
 *  5 域名转发
 *       域名 nginx  网关 都要配置
 *
 *  引入热启动 devtool
 *  关闭thymeleaf 缓存
 *  商城主页跳转页面
 *
 *  检索服务与点击分类服务
 *
 *
 *
 *
 */


@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}