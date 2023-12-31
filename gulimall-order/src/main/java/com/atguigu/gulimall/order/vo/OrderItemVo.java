package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Data
public class OrderItemVo {

    private  Long skuId;
    private  Boolean check ;
    private  String title ="";
    private  String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;


    private List<String> skuAttrValues;
    /** 商品重量 **/
    private BigDecimal weight = new BigDecimal("0.085");


}
