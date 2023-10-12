package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitController {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMessage")
    public String sendMessage()
    {

        OrderReturnReasonEntity res = new OrderReturnReasonEntity();

        for(int i = 0 ; i< 2; i++)
        {

            res.setId(Integer.toUnsignedLong(i));
            res.setName("haha" + i);
            if (i == 1)
                rabbitTemplate.convertAndSend("exchange-java", "queue-java1", res);
            else
                rabbitTemplate.convertAndSend("exchange-java", "queue-java", res);
        }


        return "ok";


    }
}
