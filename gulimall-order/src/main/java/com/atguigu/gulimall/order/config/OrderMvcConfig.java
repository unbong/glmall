package com.atguigu.gulimall.order.config;


import com.atguigu.gulimall.order.interceptor.OrderIntercepter;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderMvcConfig implements WebMvcConfigurer {



    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new OrderIntercepter()).addPathPatterns("/**");

    }
}
