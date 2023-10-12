package com.atguigu.gulimall.ware;

import com.atguigu.gulimall.ware.service.WareSkuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.aop.framework.autoproxy.target.QuickTargetSourceCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallWareApplicationTests {


    @Autowired
    AmqpAdmin amqpAdmin;
    @Test
    public void contextLoads() {


        Queue queue =new Queue("stock.release.stock.queue", true, false, false, null);
        amqpAdmin.declareQueue(queue);
    }

}
