package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

//    @Autowired
//    AmqpAdmin amqpAdmin;
//
//    @Autowired
//    RabbitTemplate rabbitTemplate;
//
    @Test
    public void contextLoads() {
    }
//
//
//    @Test
//    public void createContext()
//    {
//        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
//        DirectExchange exchange = new DirectExchange("exchange-java", true, false, null);
//        amqpAdmin.declareExchange(exchange);
//    }
//
//    @Test
//    public void createQueue()
//    {
//        // String name, boolean durable, boolean exclusive, boolean autoDelete
//        Queue queue = new Queue("queue-java", true, false, false);
//
//        amqpAdmin.declareQueue(queue);
//    }
//
//    @Test
//    public void createBinding()
//    {
//        /// String destination, DestinationType destinationType, String exchange, String routingKey,
//        Binding binding = new Binding("queue-java",
//                Binding.DestinationType.QUEUE,
//                "exchange-java", "queue-java", null);
//        amqpAdmin.declareBinding(binding);
//    }
//
//
//    @Test
//    public void sendMeessage()
//    {
//        // String exchange, String routingKey, Object message
//        OrderReturnReasonEntity ent = new OrderReturnReasonEntity();
//        ent.setCreateTime(new Date());
//        ent.setName("hello");
//        ent.setId(1L);
//        rabbitTemplate.convertAndSend("exchange-java", "queue-java", ent);
//
//
//    }

}
