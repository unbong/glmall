package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberServiceFeign {

    @RequestMapping("/member/memberreceiveaddress/getAddressByUserId/{memberid}")
    public R getAddressByUserId(@PathVariable("memberid") Long memberId);
}
