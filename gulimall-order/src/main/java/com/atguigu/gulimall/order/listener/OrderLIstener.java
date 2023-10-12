package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderLIstener {

    @Autowired
    OrderService orderService;

    /**
     * 订单状态为未支付时进行取消处理
     *
     * @param order
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void handleOrderRelease(OrderEntity order, Message message, Channel channel) throws IOException {
        log.info("收到过期的订单信息，准备关闭订单");
        Long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try{
            orderService.closeOrder(order);
            channel.basicAck(deliveryTag, false);
        }catch (Exception e)
        {
            channel.basicReject(deliveryTag, true);
        }

    }
}