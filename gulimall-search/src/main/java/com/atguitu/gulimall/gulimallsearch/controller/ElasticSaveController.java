package com.atguitu.gulimall.gulimallsearch.controller;


import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguitu.gulimall.gulimallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    // TODO: 2021/8/17  get data and call service
    // make service folder

    // call restHighlevel client
        // 在ES中建立索引， 建立好映射关系
       //  使用批量操作， 建立索引
       // BulkRequest
            // for each skuList make indexRequestr

    // 获取到service的返回值后 ，检查状态。
    //  如果出异常 则 写入自定义一唱歌信息
    //

    // 远程抵用成功 修改spu 上传状态信息

    //
    @Autowired
    private ProductSaveService elasticSaveService;

    @PostMapping("/product")
    public R saveProduct(@RequestBody List<SkuEsModel> skuEsmodels)
    {
        Boolean isOk = false;
        try{
            isOk = elasticSaveService.saveProduct(skuEsmodels);

        }catch (Exception e)
        {
            log.error("商品商家失败");
        }

        if (isOk)
        {
            return R.ok();

        }else {
            return R.error(BizCodeEnume.ELASTIC_SAVE_EXCEPTION.getCode(), BizCodeEnume.ELASTIC_SAVE_EXCEPTION.getMsg());

        }

    }
}
