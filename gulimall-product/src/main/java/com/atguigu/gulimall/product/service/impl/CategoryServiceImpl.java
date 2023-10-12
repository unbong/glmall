package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;


    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());




        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */


    @Transactional
    @Override
    @CacheEvict(value={"category"}, allEntries = true)
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    public List<CategoryEntity> getCategoryLevel1() {

        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_cid", 0);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Cacheable(value = {"category"}, key = "#root.methodName")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {


//         1 空结果缓存，解决缓存穿透
//         2   设置过期时间（随机值）
//
//          3枷锁，解决缓存击穿
        /**
         *
         *
         *
         */

//        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
//        String cataLogJson =  ops.get("catalogJson");
//
//        Map<String, List<Catalog2Vo>> cate3Logs = null;

//        if (StringUtils.isEmpty(cataLogJson))
//        {
//            cate3Logs =  this.catalog3LevelDB();
//        }
//        else
//        {
//            cate3Logs = JSON.parseObject(cataLogJson,new TypeReference<Map<String, List<Catalog2Vo>>>(){});
//        }
        return  this.catalog3LevelDB();
    }

    private List<CategoryEntity> getCategoryByParentCid(List<CategoryEntity> list, Long catId)
    {
        return list.stream().filter(
                item->{
                   return item.getParentCid() == catId;
                }
        ).collect(Collectors.toList());

    }



    private   Map<String, List<Catalog2Vo>> catalog3LevelDB(){

//        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
//        String cataLogJson =  ops.get("catalogJson");
        Map<String, List<Catalog2Vo>> cate3Logs = null  ;
        System.out.println("read db");
        // 本地实例锁
//        synchronized (this)
//        {
//            if (StringUtils.isEmpty(cataLogJson))
//            {
//                List<CategoryEntity> cate1List = this.list();
//
//                cate3Logs = cate1List.stream().collect(Collectors.toMap(
//                        key->key.getCatId().toString(),
//                        value->{
//                            List<Catalog2Vo> catlogLevel2Vo = null;
//                            List<CategoryEntity> catlog2List = this.getCategoryByParentCid(cate1List, value.getCatId());
//                            if (catlog2List == null ) return catlogLevel2Vo;
//
//                            catlogLevel2Vo = catlog2List.stream().map(l2->{
//
//                                Catalog2Vo catelog2 = new Catalog2Vo(l2.getParentCid().toString(), null , l2.getCatId().toString(), l2.getName());
//
//                                List<Catalog2Vo.Catalog3Vo> catelog3vos = null;
//                                List<CategoryEntity> catelog3list = this.getCategoryByParentCid(cate1List, l2.getCatId());
//
//                                if (catelog3list == null) return null;
//                                catelog3vos = catelog3list.stream().map(
//                                        l3-> {
//                                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(
//                                                    l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
//                                            return catalog3Vo;
//                                        }
//
//                                ).collect(Collectors.toList());
//
//                                catelog2.setCatalog3List(catelog3vos);
//                                return catelog2;
//
//                            }).collect(Collectors.toList());
//                            return catlogLevel2Vo;
//                        }
//                ));
//                String s = JSON.toJSONString(cate3Logs);
//                ops.set("catalogJson", s,1 , TimeUnit.DAYS);
//
//                return cate3Logs;
//            }
//
//            cate3Logs =  JSON.parseObject( cataLogJson, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
//        }
//
//        if (StringUtils.isEmpty(cataLogJson))
//        {
            List<CategoryEntity> cate1List = this.list();

            cate3Logs = cate1List.stream().collect(Collectors.toMap(
                    key->key.getCatId().toString(),
                    value->{
                        List<Catalog2Vo> catlogLevel2Vo = null;
                        List<CategoryEntity> catlog2List = this.getCategoryByParentCid(cate1List, value.getCatId());
                        if (catlog2List == null ) return catlogLevel2Vo;

                        catlogLevel2Vo = catlog2List.stream().map(l2->{

                            Catalog2Vo catelog2 = new Catalog2Vo(l2.getParentCid().toString(), null , l2.getCatId().toString(), l2.getName());

                            List<Catalog2Vo.Catalog3Vo> catelog3vos = null;
                            List<CategoryEntity> catelog3list = this.getCategoryByParentCid(cate1List, l2.getCatId());

                            if (catelog3list == null) return null;
                            catelog3vos = catelog3list.stream().map(
                                    l3-> {
                                        Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(
                                                l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                                        return catalog3Vo;
                                    }

                            ).collect(Collectors.toList());

                            catelog2.setCatalog3List(catelog3vos);
                            return catelog2;

                        }).collect(Collectors.toList());
                        return catlogLevel2Vo;
                    }
            ));
//            String s = JSON.toJSONString(cate3Logs);
//            ops.set("catalogJson", s,1 , TimeUnit.DAYS);

            return cate3Logs;
//        }


//        return cate3Logs;
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }



}