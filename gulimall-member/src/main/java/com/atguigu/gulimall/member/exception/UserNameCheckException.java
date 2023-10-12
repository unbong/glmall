package com.atguigu.gulimall.member.exception;

public class UserNameCheckException extends RuntimeException{

    public UserNameCheckException()
    {
        super("用户名已经存在");
    }
}
