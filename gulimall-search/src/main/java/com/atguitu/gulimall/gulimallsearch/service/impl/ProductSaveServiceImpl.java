package com.atguitu.gulimall.gulimallsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguitu.gulimall.gulimallsearch.config.GulimallElasticSearchConfig;
import com.atguitu.gulimall.gulimallsearch.constant.EsConstant;
import com.atguitu.gulimall.gulimallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("ProductSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean saveProduct(List<SkuEsModel> skuEsmodels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel skuEsmodel : skuEsmodels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.ELASTIC_INDEX_PRODUCT);
            String source = JSON.toJSONString(skuEsmodel);
            indexRequest.id(skuEsmodel.getSkuId().toString());
            indexRequest.source(source, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        boolean hasFailures = bulkResponse.hasFailures();
        List<String> collect = Arrays.asList(bulkResponse.getItems()).stream().map(item->{
            return item.getId();
        }).collect(Collectors.toList());

        log.info("商品商家完成 {}", collect);


        return !hasFailures;
    }
}
