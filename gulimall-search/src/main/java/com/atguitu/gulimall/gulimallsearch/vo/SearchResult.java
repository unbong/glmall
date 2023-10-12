package com.atguitu.gulimall.gulimallsearch.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {


    /**
     *  skuModel 集合
     *  当前页么
     *  总页码
     *
     *  品牌集合  当前查询到的结果 所有涉及到的品牌
     *      ID
     *      名称
     *      图片地址
     *
     *  属性集合  符合所有商品具有的属性  当前查询道德所有
     *      属性iD
     *      属性名
     *      属性的值的集合
     *
     *  分类的集合
     *      分类ID
     *      分类名字
     *
     *
     *      以上返回给页面的所有信息
     *
     *
     *
     *  elastic search 的映射修改
     *  将index 与doc value的设定删除
     *
     *  数据的迁移
     *
     *      在聚合结果中获取其他结果， 利用子聚合的功能
     *      1 新建index
     *      2 post _reindex
     *      {
     *          from
     *          dest...
     *      }
     *
     *
     *
     */


    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> product;


    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;

    /**
     * 当前查询到的结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询到的结果，所有涉及到的所有分类
     */
    private List<CatalogVo> catalogs;


    //===========================以上是返回给页面的所有信息============================//


    /* 面包屑导航数据 */
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    @AllArgsConstructor
    public static class BrandVo {

        private Long brandId;

        private String brandName;

        private String brandImg;
    }


    @Data
    @AllArgsConstructor
    public static class AttrVo {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }


    @Data
    @AllArgsConstructor
    public static class CatalogVo {

        private Long catalogId;

        private String catalogName;
    }



}
