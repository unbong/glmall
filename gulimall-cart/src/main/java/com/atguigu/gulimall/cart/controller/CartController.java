package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String getCartList(Model model)
    {

        CartVo cartVo=cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }


    @GetMapping("/redirectToSuccess")
    public String redirectToSuccess(@RequestParam("skuId") Long skuId,  Model model)  {

        CartItemVo item = cartService.getCartItem(skuId);

        model.addAttribute("item",item);
        return "success";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("itemCount") Integer itemCount , RedirectAttributes ra) throws ExecutionException, InterruptedException {
        CartItemVo item = cartService.addToCart(skuId, itemCount);
        ra.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/redirectToSuccess";
    }


    @GetMapping("/changeCount")
    public String changeCount(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count){

        cartService.changeCount(skuId, count);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId)
    {
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @PostMapping("/checkCart")
    public String changeItemCheck(@RequestParam("skuId")Long skuId, @RequestParam("isChecked") Integer isChecked){


        cartService.changeItemCheck(skuId, isChecked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @ResponseBody
    @GetMapping("/getCheckItems")
    public List<CartItemVo> getCheckItems()
    {
        List<CartItemVo> items = cartService.getCheckItems();
        return items;
    }

}
