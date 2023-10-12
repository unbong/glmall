package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartVo getCart();

    CartItemVo addToCart(Long skuId, Integer itemCount) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    void changeItemCheck(Long skuId, Integer isChecked);

    void changeCount(Long skuId, Integer count);

    void deleteItem(Long skuId);

    List<CartItemVo> getCheckItems();
}
