package com.atguigu.gulimall.ware.service;


import com.atguigu.common.to.OrderTo;
import com.atguigu.gulimall.ware.to.StockLockTO;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-08 09:59:40
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    List<SkuHasStockVo> getHasStock(List<Long> skuIds) ;

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);


    Boolean orderLockStock(WareSkuLockVo wareSkuLockVo);

    void unLock(StockLockTO lockTO);

    void unLock(OrderTo lockTO);
}

