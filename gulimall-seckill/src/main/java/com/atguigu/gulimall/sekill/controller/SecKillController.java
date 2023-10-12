package com.atguigu.gulimall.sekill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.sekill.service.SecKillService;
import com.atguigu.gulimall.sekill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    SecKillService secKillService;

    @SentinelResource
    @ResponseBody
    @RequestMapping("/getCurrentSecKill")
    public R getCurrentSecKill()
    {
        List<SeckillSkuRedisTo> result = secKillService.getCurrentSecKill();
        return R.ok().put("data",result);
    }

    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId)
    {
        SeckillSkuRedisTo to =  secKillService.getSeckillSkuInfo(skuId);
       return R.ok().put("data" , to);
    }

    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId,
                       @RequestParam("key") String key,
                       @RequestParam("num") Integer num)
    {

        String orderId =   secKillService.secKll(killId, key, num);
        return "success";
    }
}
