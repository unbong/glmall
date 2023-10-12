package com.atguitu.gulimall.gulimallsearch.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface AttrFeign {

    @RequestMapping("/product/attr/info/{attrId}")
    //@RequiresPermissions("/product/attr/product:attr:info")
    public R info(@PathVariable("attrId") Long attrId);
}
