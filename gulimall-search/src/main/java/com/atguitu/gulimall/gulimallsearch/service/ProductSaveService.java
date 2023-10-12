package com.atguitu.gulimall.gulimallsearch.service;


import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface ProductSaveService {
    Boolean saveProduct(List<SkuEsModel> skuEsmodels) throws IOException;
}
