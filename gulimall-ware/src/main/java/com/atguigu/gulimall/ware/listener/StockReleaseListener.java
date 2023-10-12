package com.atguigu.gulimall.ware.listener;


import com.atguigu.common.to.OrderTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.to.StockLockTO;
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
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockTO lockTO , Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        try{
            wareSkuService.unLock(lockTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        catch (Exception e)
        {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }


    }


    @RabbitHandler
    public void handleStockLockedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("************************从订单模块收到库存解锁的消息********************************");
        try
        {
            wareSkuService.unLock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }catch ( Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }






}
