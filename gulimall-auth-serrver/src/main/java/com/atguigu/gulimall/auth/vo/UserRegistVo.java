package com.atguigu.gulimall.auth.vo;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {

    /**
     * Entity验证追加
     *
     * 验证完成后 BindingResult返回校验信息
     *   BindingResult的getFieldError，通过对其流氏变成 改编成Map key俄日field 值为错误信息
     *
     *  thymeleaf 返回值为map时 通过. 表达式能够取出对应key的值
     *      #maps.containKey(map， key) 来判断key存在与否
     *  Request Method Post not supported
     *
     *  注册页面-》 post请求 -》 转发-》到注册页面（ 转发的默认请求反式维get 导致的上米娜的 ）
     *
     * 利用Session原理 将数据放到Session中，只要跳到下一个页面Session数据取出后，Session里面的数据就会被删除
     * 如果利用Session 会遇到分布式Session问题。
     *  表单重复提交解决的方式是使用重定向
     *      重定向的目的地是页面地址（）
     *
     *  重定向 重定向是无法携带数据
     *      RedirectAttibute 模拟重定向携带数据
     *             AddFlashAttribute 将数据返回到重定向后的也
     *
     */


    /**
     * 注册使用 远程服务
     *    判断验证码 是否有效
     *
     *  远程调用MemberController
     *
     *  初始话的数据
     *      查询默认等级
     *       检查用户名与手机是否唯一
     *   为了让Contriller感知异常，可以抛出不同的异常
     *
     *
     *   MD5 盐值加密
     *      BCryptPassWordEncoder
     *
     */

    /**
     *
     *
     *
     登录功能
     登录用Vo
     登录成功后重定向到商城首页
     发送远程登录请求
     利用BCryptXXXEncode中的match方法来验证密码是否正确

     查询用户名与手机是都一样


     社交登录

     获取ACCES——token之后如果是第一次登录，自动注册用户，当前社交账号用户生成一个信息账号。
     调用远程方法 接收社交ID来判断登录或者注册
     member表中添加社交ID与过期时间 令牌的字段。

     没有查到社交ID关联的数据，根据社交上的相关信息，来补充用户的信息
     昵称
     性别

     */

    /**
     *
     * 使用Session来存储跨网页数据共享 ，在微服务
     *      HTTPSession
     */






    @NotEmpty(message = "用户名不能为空")
    @Length(min=6,max=19,message = "用户名长度在6—18字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min=6,max=19,message = "密码必须是6—18字符")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
