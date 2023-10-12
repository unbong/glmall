package com.atguigu.gulimall.auth.vo;

import lombok.Data;

@Data
public class SocialUser {


    private String idtoken;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;

    private String name ;



}
