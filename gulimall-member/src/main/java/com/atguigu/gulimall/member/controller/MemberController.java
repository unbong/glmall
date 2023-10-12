package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.member.exception.PhoneCheckException;
import com.atguigu.gulimall.member.exception.UserNameCheckException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.LoginUserVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:47:05
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;


    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
    }



    @PostMapping("/login")
    public R login(@RequestBody LoginUserVo vo)
    {

        MemberEntity memberEntity =  memberService.login(vo);
        if(memberEntity == null)
        {
            return   R.error(BizCodeEnume.LOGIN_ACCT_PASS_EXCEPTION.getCode(), BizCodeEnume.LOGIN_ACCT_PASS_EXCEPTION.getMsg());

        }else{

             return R.ok().put("memberEntity", memberEntity);
        }
    }

    /**
     *
     *
     */
    @PostMapping("regist")
    public R registUser(@RequestBody UserRegistVo vo)
    {
        /**
         *  验证码
         */

        try {
            memberService.registUser(vo);
        }catch (UserNameCheckException e)
        {
            // UserName 已存在
            return R.error(BizCodeEnume.USERNAME_CHECK_EXCEPION.getCode(), BizCodeEnume.USERNAME_CHECK_EXCEPION.getMsg());
        }catch (PhoneCheckException e)
        {

            return R.error(BizCodeEnume.PHONE_CHECK_EXCEPION.getCode(), BizCodeEnume.PHONE_CHECK_EXCEPION.getMsg());
        }

        return R.ok();

    }

    @PostMapping("/social/login")
    public R loginSocial(@RequestBody SocialUser user) {

        MemberEntity memberEntity = memberService.loginSocial(user);

        if(memberEntity != null)
        {
            return R.ok().put("memberEntity", memberEntity);
        }else{
            return R.error(BizCodeEnume.SOCIA_USER_LOGIN_EXCEPTION.getCode(), BizCodeEnume.SOCIA_USER_LOGIN_EXCEPTION.getMsg());

        }

    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
