package com.atguigu.gulimall.auth.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.feign.MemverFeign;
import com.atguigu.gulimall.auth.service.LoginService;
import com.atguigu.gulimall.auth.vo.LoginUserVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import com.netflix.ribbon.proxy.annotation.Http;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginControler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemverFeign memverFeign;

    @Autowired
    private LoginService loginService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object loginSession =  session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(loginSession == null)
        {
            return "login";
        }
        else {

            return "redirect:http://gulimall.com";
        }

    }

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        String code = "12345";
        String smsCache = redisTemplate.opsForValue().get(phone);
        if(StringUtils.isEmpty(smsCache)){

            String value = code +"_" +System.currentTimeMillis();
            redisTemplate.opsForValue().set(phone, value,10 , TimeUnit.MINUTES);
        }
        else {
            Long dateTime = Long.parseLong(smsCache.split("_")[1]) ;

            if (System.currentTimeMillis() - dateTime< 60000 ){
               return R.error(BizCodeEnume.SMS_CODE_ALIVE.getCode(),BizCodeEnume.SMS_CODE_ALIVE.getMsg() );
            }
        }
        // 否则发送新的短信请求



        return R.ok();
    }


    @PostMapping("/login2")
    public String login( LoginUserVo vo , RedirectAttributes attributes, HttpSession session){

        Map<String,String > errors = new HashMap<>();
        R r = loginService.login(vo);
        if(r.getCode() ==0)
        {
            MemberResponseVo member = r.getData("memberEntity", new TypeReference<MemberResponseVo>(){});
            attributes.addFlashAttribute("loginData", member);
            session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        }else
        {

            errors.put("msg", (String)r.get("msg"));
            attributes.addFlashAttribute("errors",errors);

        }

        return "redirect:http://gulimall.com";

    }



    /**
     * 注册
     *  初测成功 返回首页
     *
     *  准备vo
     *
     *
     */

    @GetMapping("/login")
    public String login(@RequestParam("idtoken") String  idtoken, RedirectAttributes redirectAttributes,  HttpSession session) {

        Map<String, String> errors = new HashMap<>();

//        if (request == null || !request.containsKey("idtoken") )
//        {
//            errors.put("msg", "请求数据异常");
//            redirectAttributes.addFlashAttribute("errors",errors);
//            return "redirect:http://auth.gulimall.com/login.html";
//        }

        SocialUser loginUserVo = new SocialUser();
        //loginUserVo.setIdtoken( request.get("idtoken").get(0) );
        loginUserVo.setIdtoken(idtoken);
        try {
            boolean isValidUser = loginService.login(loginUserVo);
            if(isValidUser)
            {
                R r = memverFeign.loginSocial(loginUserVo);
                if(r.getCode() ==0)
                {
                    MemberResponseVo vo =  r.getData("memberEntity", new TypeReference<MemberResponseVo>(){});
                    redirectAttributes.addFlashAttribute("member", vo);
                    session.setAttribute(AuthServerConstant.LOGIN_USER, vo);
                    return "redirect:http://gulimall.com";
                }
                else{
                   String msg =(String) r.get("msg");
                    errors.put("msg", msg);
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/login.html";
                }


            }


        } catch (GeneralSecurityException e) {
            errors.put("msg", e.getMessage());
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        } catch (IOException e) {
            errors.put("msg", e.getMessage());
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

        return "redirect:http://gulimall.com";
    }


    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo registVo, BindingResult result,
                         RedirectAttributes redirectAttributes)
    {
        Map<String, String> errors = new HashMap<>();
        if(result.hasErrors())
        {
            errors= result.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError ->  fieldError.getField(),
                    fieldError-> fieldError.getDefaultMessage()
            ));

            redirectAttributes.addFlashAttribute("errors",errors);

            return "redirect:http://auth.gulimall.com/reg.html";
        }
        else{

            String code_time =redisTemplate.opsForValue().get(registVo.getPhone());

            if(StringUtils.isEmpty(code_time))
            {
                errors.put("code", "验证码已失效");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }else
            {
                String code = code_time.split("_")[0];
                if(code.equals(registVo.getCode()))
                {
                    redisTemplate.delete(registVo.getPhone());
                    R r = memverFeign.registUser(registVo);

                    if( r.getCode() == 0){
                        return "redirect:http://auth.gulimall.com/login.html";
                    }
                    else{

                        String msg = (String)r.get("msg");
                        errors.put("msg", msg);
                        redirectAttributes.addFlashAttribute("errors",errors);
                        return "redirect:http://auth.gulimall.com/reg.html";
                    }

                }else{
                    errors.put("code", "验证码错误");
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }



        }

    }

}
