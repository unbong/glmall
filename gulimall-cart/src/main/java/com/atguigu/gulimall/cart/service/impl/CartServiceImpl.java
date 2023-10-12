package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl  implements CartService {

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public CartVo getCart() {

        BoundHashOperations redisHashOperation = getBoundHashOperartions();
        UserInfoTo userInfo =  CartInterceptor.threadLocal.get();
        CartVo cart = new CartVo();
        List<CartItemVo> itemList  = getCartByKey(CartConstant.CART_PREFIX + userInfo.getUserKey());

//        已登录
        if(userInfo.getUserId()!= null)
        {
            //merge cart
            if(itemList != null)
            {
                for (CartItemVo cartItemVo : itemList) {

                    String itemStr = (String)redisHashOperation.get(cartItemVo.getSkuId().toString());
//                    已存在于Cart中
                    if (!Strings.isEmpty(itemStr))
                    {
                        CartItemVo item = JSON.parseObject(itemStr, CartItemVo.class);
                        item.setCount(item.getCount() + cartItemVo.getCount());
                        String s = JSON.toJSONString(item);
                        redisHashOperation.put(item.getSkuId().toString(), s);
                    }
                    else{

                        String s = JSON.toJSONString(cartItemVo);
                        redisHashOperation.put(cartItemVo.getSkuId().toString(), s);

                    }
                }
            }
            List<CartItemVo> itemsInId  = getCartByKey(CartConstant.CART_PREFIX + userInfo.getUserId());
            cart.setItems(itemsInId);
            redisTemplate.delete(CartConstant.CART_PREFIX + userInfo.getUserKey());
        }else
        {
            if(itemList != null)
                cart.setItems(itemList);

        }

        return cart;
    }

    @Nullable
    private List<CartItemVo> getCartByKey(String key)
    {

        BoundHashOperations redisHashOperation= redisTemplate.boundHashOps(key);
        List<Object> items = redisHashOperation.values();
        if (items != null && items.size()> 0)
        {
            List<CartItemVo> itemList =  items.stream().map(obj->{
                String  s = (String)obj;
                CartItemVo item = JSON.parseObject(s, CartItemVo.class);
                return item;
            }).collect(Collectors.toList());

            return itemList;
        }
        return  null;
    }

    @Override
    public CartItemVo addToCart(Long skuId, Integer itemCount) throws ExecutionException, InterruptedException {

        // 获取redis操作 以cart用户信息为key
        BoundHashOperations redisHashOperation = getBoundHashOperartions();


        String  item = (String) redisHashOperation.get(skuId.toString());
        if(!Strings.isEmpty(item))
        {
            CartItemVo cart = JSON.parseObject(item,CartItemVo.class);
            cart.setCount(cart.getCount() + itemCount);
            String itemStr = JSON.toJSONString(cart);

            redisHashOperation.put(skuId.toString(), itemStr);
            return cart;
        }
        else{
            CartItemVo cartItem = new CartItemVo();
            // 1 异步获取sku信息
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(()->{
                R r = productFeignService.info(skuId);
                if(r.getCode() ==0)
                {
                    SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>(){});
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setSkuId(skuId);
                    cartItem.setCount(itemCount);
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setTitle(skuInfo.getSkuTitle());

                }

            },executor );

            // 2 异步获取销售属性信息
            CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(()->{

                cartItem.setSkuAttr( productFeignService.getSkuSaleAttrValuesAsString(skuId));
            }, executor);

            CompletableFuture.allOf(skuInfoFuture, skuAttrFuture).get();

            String s = JSON.toJSONString(cartItem);
            //        存储到redis中
            redisHashOperation.put(cartItem.getSkuId().toString(), s);

            return cartItem;
        }


    }

    @Override
    public CartItemVo getCartItem(Long skuId) {

        BoundHashOperations redisOps = getBoundHashOperartions();
        String s = (String)redisOps.get(skuId.toString());
        CartItemVo item = JSON.parseObject(s,  CartItemVo.class);
        return item;
    }

    @Override
    public void changeItemCheck(Long skuId, Integer isChecked) {

        BoundHashOperations redisOps = getBoundHashOperartions();
        String s = (String) redisOps.get(skuId.toString());
        if(!Strings.isEmpty(s))
        {
            CartItemVo item = JSON.parseObject(s, CartItemVo.class);
            item.setCheck(isChecked == 1);

            String itemStr = JSON.toJSONString(item);
            redisOps.put(skuId.toString(), itemStr);
        }

    }

    @Override
    public void changeCount(Long skuId, Integer count) {
        BoundHashOperations redisOps = getBoundHashOperartions();
        String s = (String) redisOps.get(skuId.toString());
        if(!Strings.isEmpty(s))
        {
            CartItemVo item = JSON.parseObject(s, CartItemVo.class);
            item.setCount(count);

            String itemStr = JSON.toJSONString(item);
            redisOps.put(skuId.toString(), itemStr);
        }
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations redisOps = getBoundHashOperartions();
        redisOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCheckItems() {

        UserInfoTo userInfo =  CartInterceptor.threadLocal.get();

        List<CartItemVo > items = getCartByKey( CartConstant.CART_PREFIX + userInfo.getUserId().toString());


        // todo 获取商品实际的价格没有做
        List<CartItemVo > result = items.stream().filter(item->item.getCheck()).collect(Collectors.toList());

        return result;
    }

    private BoundHashOperations getBoundHashOperartions() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        String key = CartConstant.CART_PREFIX;
        // 如果用户登录
        if (userInfo.getUserId() != null)
        {
            key = key + userInfo.getUserId();
        }
        //        用户没登陆
        else if ( !Strings.isEmpty(userInfo.getUserKey()))
        {
            key = key + userInfo.getUserKey();
        }

        return redisTemplate.boundHashOps(key);
    }


}
