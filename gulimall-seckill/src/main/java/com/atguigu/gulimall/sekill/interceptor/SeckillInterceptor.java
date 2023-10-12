package com.atguigu.gulimall.sekill.interceptor;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResponseVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SeckillInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean match = pathMatcher.match("/kill", uri);
        if (match) {
            HttpSession session = request.getSession();
            MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);


            if (member == null) {
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            } else {
                loginUser.set(member);
                return true;

            }


        }

        return true;
    }
}