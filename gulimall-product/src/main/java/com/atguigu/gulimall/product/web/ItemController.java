package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class ItemController {

    /**
     *
     *
     *
     * @param skuId
     * @param model
     * @return
     */

    @Autowired
    private SkuInfoService skuInfoService;



    @GetMapping("/{skuId}.html")
    public String itemIndex(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {


        /**
         *  1 sku基本信息
         *
         *  2 sku图片信息
         *
         *
         *  4 spu 介绍
         *
         *     mybatis的嵌套属性的封装 需要自定义的
         */

        SkuItemVo vo = skuInfoService.Item(skuId);
        model.addAttribute("item", vo);
        return "item";
    }
}
