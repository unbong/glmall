package com.atguigu.gulimall.product.feign;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.CallBack.SecKillFeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value = "gulimall-seckill",fallback = SecKillFeignServiceImpl.class)
public interface SecKillFeignService {


    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    R getSeckillSkuInfo(@PathVariable("skuId") Long skuId);
}
