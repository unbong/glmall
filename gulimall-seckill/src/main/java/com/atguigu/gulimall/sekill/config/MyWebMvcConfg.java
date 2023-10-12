package com.atguigu.gulimall.sekill.config;


import com.atguigu.gulimall.sekill.interceptor.SeckillInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebMvcConfg  implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new SeckillInterceptor()).addPathPatterns("/**");

    }
}
