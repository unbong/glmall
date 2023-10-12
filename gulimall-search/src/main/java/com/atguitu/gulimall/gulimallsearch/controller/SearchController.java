package com.atguitu.gulimall.gulimallsearch.controller;

import com.atguitu.gulimall.gulimallsearch.service.MallSearchService;
import com.atguitu.gulimall.gulimallsearch.vo.SearchParam;
import com.atguitu.gulimall.gulimallsearch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {


    /**
     *
     *      controller 获取请求
     *      param vo传递参数
     *          keyword 全文匹配
     *          catalog3Id 三级分类
     *          排序 按照销量， 热度评分 价格
     *
     *          cataLog3Id=225&keyword=XM&sort=sacleCount_ASC/DESC
     *
     *      service 处理请求
     *
     *
     *
     *
     *
     * @return
     */

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping({"/", "search.html"})
    public String getSearchPage(SearchParam param , Model model, HttpServletRequest servletRequest)
    {
        param.set_queryString( servletRequest.getQueryString());
        SearchResult  result =  mallSearchService.search(param);
        model.addAttribute("result", result);
        return "search";
    }
}
