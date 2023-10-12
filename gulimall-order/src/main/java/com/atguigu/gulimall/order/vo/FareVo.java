package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberAddressVo address;

    private BigDecimal fare;
}

