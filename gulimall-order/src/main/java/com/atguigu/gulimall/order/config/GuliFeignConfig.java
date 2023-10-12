package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {


    @Bean
    public RequestInterceptor requestInterceptor(){

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 通过RequestContextHolder获取当前请求的内容（老请求）

                ServletRequestAttributes RequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(RequestAttributes!= null)
                {

                    HttpServletRequest httpRequest = RequestAttributes.getRequest();
                    if (httpRequest != null)
                    {
                        // 将老请求的cookie信息放到Feign请求头中
                        String cookie = httpRequest.getHeader("Cookie");
                        template.header("Cookie", cookie);
                    }

                }



            }
        };
    }
}
