package com.atguigu.gulimall.sekill.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecKillSentinelConfig   {

    public SecKillSentinelConfig()
    {
        WebCallbackManager.setUrlBlockHandler(((httpServletRequest, httpServletResponse, e) -> {

            R r = R.error(BizCodeEnume.TOO_MANY_REQUET.getCode(), BizCodeEnume.TOO_MANY_REQUET.getMsg());
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(JSON.toJSONString(r.toString()));

        }));
    }
}
