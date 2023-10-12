package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.SecKillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RabbitListener(queues = {"order.seckill.order.queue"})
public class SecKillOrderListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleSecKillOrder(SecKillOrderTo secKillOrderTo, Message message, Channel channel) throws IOException {
        log.info("接收到秒杀订单消息");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try{
            orderService.createSecKillOrder(secKillOrderTo);
            channel.basicAck(deliveryTag, false);
        }
        catch(Exception e)
        {
            channel.basicReject(deliveryTag,true);

        }

    }
}
