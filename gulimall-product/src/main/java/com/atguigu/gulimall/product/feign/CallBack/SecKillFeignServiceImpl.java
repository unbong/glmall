package com.atguigu.gulimall.product.feign.CallBack;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SecKillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecKillFeignServiceImpl implements SecKillFeignService {

    @Override
    public R getSeckillSkuInfo(Long skuId) {
        log.info("************熔断被触发***************");
        return R.error().put(BizCodeEnume.TOO_MANY_REQUET.getMsg(), BizCodeEnume.TOO_MANY_REQUET.getCode());
    }
}
