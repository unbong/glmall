package com.atguigu.gulimall.auth.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.LoginUserVo;
import com.atguigu.gulimall.auth.vo.SocialUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface LoginService {

    boolean login(SocialUser loginUserVo) throws GeneralSecurityException, IOException;

    R login(LoginUserVo vo);
}
