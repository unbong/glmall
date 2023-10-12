package com.atguigu.gulimall.auth.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUserVo implements Serializable {


    private String username;
    private String password;


}
