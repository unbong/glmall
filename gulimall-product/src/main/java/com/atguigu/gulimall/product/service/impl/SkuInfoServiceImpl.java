package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.config.MyThreadExcutor;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SecKillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillSkuVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {



    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValue;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SecKillFeignService secKillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper)->{
               wrapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){

            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            queryWrapper.ge("price",min);
        }

        String max = (String) params.get("max");

        if(!StringUtils.isEmpty(max)  ){
            try{
                BigDecimal bigDecimal = new BigDecimal(max);

                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.le("price",max);
                }
            }catch (Exception e){

            }

        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return this.list(wrapper);

    }

    @Override
    public SkuItemVo Item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo vo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture =  CompletableFuture.supplyAsync(() -> {
            // 1  sku 基本信息 pms sku info
            SkuInfoEntity info = baseMapper.selectById(skuId);
            vo.setInfo(info);
            return info;
        }, executor );

        CompletableFuture<Void> saleAttrFuture =  infoFuture.thenAcceptAsync((res)->{
            //3 销售属性

            List<SkuItemSaleAttrVo> vos = skuSaleAttrValue.getSaleAttrVos(res.getSpuId());
            vo.setSaleAttr(vos);


        },executor);

        CompletableFuture<Void> spuFuture =infoFuture.thenAcceptAsync((res)->{
            //4 获取spu的介绍
            SpuInfoDescEntity infoDesc = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(infoDesc);

        }, executor);

        CompletableFuture<Void> attrFuture =infoFuture.thenAcceptAsync((res)->{
            //5 spu规格参数
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos=
                    productAttrValueService.getProductGroupAttrsBySpuId(res.getSpuId(),res.getCatalogId() );

            vo.setGroupAttrs(spuItemAttrGroupVos);
        });


        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(()->{
            // 2 图片信息
            List<SkuImagesEntity> skuImagesEntities = imagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            vo.setImages(skuImagesEntities);
        }, executor);


        //6、秒杀商品的优惠信息
        CompletableFuture<Void> seckFuture = CompletableFuture.runAsync(() -> {
            R r = secKillFeignService.getSeckillSkuInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuVo seckillSkuVo = r.getData( "data",new TypeReference<SeckillSkuVo>() {
                });

                long current = System.currentTimeMillis();
                //如果返回结果不为空且活动未过期，设置秒杀信息
                if (seckillSkuVo != null&&current<seckillSkuVo.getEndTime()) {
                    vo.setSeckillSkuVo(seckillSkuVo);
                }
            }
        }, executor);

        CompletableFuture.allOf(infoFuture,saleAttrFuture, imgFuture,attrFuture,seckFuture, spuFuture).get();


        return vo;
    }


}