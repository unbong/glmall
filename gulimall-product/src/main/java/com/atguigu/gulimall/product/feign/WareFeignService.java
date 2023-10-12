package com.atguigu.gulimall.product.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    // TODO: 2021/8/16  add feign function

     @PostMapping("/ware/waresku/hasstock")
     public R getHasStock(@RequestBody List<Long> skuIds);



}
