package com.atguigu.gulimall.auth.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemverFeign;
import com.atguigu.gulimall.auth.service.LoginService;
import com.atguigu.gulimall.auth.vo.LoginUserVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private MemverFeign memverFeign;


    @Override
    public boolean login(SocialUser loginUserVo) throws GeneralSecurityException, IOException {


        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList("908677375004-87mnbiisk9kdh4upvr74gn2ame8fn1cu.apps.googleusercontent.com"))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
        GoogleIdToken idToken = verifier.verify(loginUserVo.getIdtoken());
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            SocialUser socialUser = new SocialUser();
            socialUser.setIdtoken(loginUserVo.getIdtoken());
            socialUser.setName(name);

            return true;


        } else {
            System.out.println("Invalid ID token.");
        }


        return false;
    }

    @Override
    public R login(LoginUserVo vo) {

        R r =memverFeign.login(vo);

        return r;
    }
}
