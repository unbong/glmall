package com.atguigu.gulimall.order.config;

import com.rabbitmq.client.ConfirmCallback;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ConvertConfig {

    //@Autowired
    RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter jsonConverter()
    {
        return new Jackson2JsonMessageConverter();
    }


    // 当消息送达到Exchane是被调用

//    @PostConstruct
//    public void setConfirmCallBack(){
//
//        // @Nullable CorrelationData correlationData, boolean ack, @Nullable String cause
//        rabbitTemplate.setConfirmCallback((correlationData, act, cause  )->{
//            System.out.println("correlation: "  + correlationData + "->    act: " + act + "->    cause: " + cause);
//        });
//
//
////        Message message, int replyCode, String replyText,
////                String exchange, String routingKey
//
//        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey)->{
//
//            System.out.println("message: "  + message.toString() + "->    replyCode: " + replyCode + "->    replyText: " + replyText +
//                   " exchange: " + exchange + "->    routingKey: " + routingKey
//                    );
//        });
//
//    }

}
