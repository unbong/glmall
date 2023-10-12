package com.atguitu.gulimall.gulimallsearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GulimallSessionConfig {

    // cookie的序列话机制配置
    @Bean
    public CookieSerializer cookieSerializer()
    {
        DefaultCookieSerializer cookie =  new DefaultCookieSerializer();
        cookie.setCookieName("GULIMALLSESSION");
        cookie.setDomainName("gulimall.com");

        return cookie;

    }

    @Bean
    public RedisSerializer<Object> redisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }

}
