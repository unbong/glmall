package com.atguigu.gulimall.member.exception;

public class PhoneCheckException extends RuntimeException{

    public PhoneCheckException()
    {
        super("手机号码已被使用");
    }
}
