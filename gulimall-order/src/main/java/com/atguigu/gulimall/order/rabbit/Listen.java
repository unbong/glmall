package com.atguigu.gulimall.order.rabbit;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {"queue-java"})
public class Listen {

    @RabbitHandler
    public void listen(OrderReturnReasonEntity order)
    {

        System.out.println("conten->" + order.toString());

    }
}
