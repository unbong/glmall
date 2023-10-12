package com.atguigu.gulimall.sekill.service;

import com.atguigu.gulimall.sekill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SecKillService {

    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSecKill();

    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);

    String secKll(String killId, String key, Integer num);
}
