package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.LoginUserVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemverFeign {

    @PostMapping("/member/member/regist")
    public R registUser(@RequestBody UserRegistVo vo);


    @PostMapping("/member/member/social/login")
    public R loginSocial(@RequestBody SocialUser user);


    @PostMapping("/member/member/login")
    public R login(@RequestBody LoginUserVo vo);
}
