package com.atguitu.gulimall.gulimallsearch.service;

import com.atguitu.gulimall.gulimallsearch.vo.SearchParam;
import com.atguitu.gulimall.gulimallsearch.vo.SearchResult;

public interface MallSearchService {

    SearchResult search(SearchParam param);
}
