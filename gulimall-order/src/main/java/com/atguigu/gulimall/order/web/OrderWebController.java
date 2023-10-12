package com.atguigu.gulimall.order.web;


import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.interceptor.OrderIntercepter;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class OrderWebController {


    @Autowired
    OrderService otderService;



    @SneakyThrows
    @GetMapping("toTrade")
    public String toTrade(Model model)
    {
        OrderConfirmVo orderConfirm =  otderService.getConfirmOrder();

        model.addAttribute("orderConfirm", orderConfirm);
        return "confirm";
    }


    @PostMapping("submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes attributes){

        try{
            SubmitOrderResponseVo responseVo =  otderService.submitOrder(submitVo);
            if(responseVo.getCode() == 0)
            {
                model.addAttribute("order", responseVo.getOrder());
                return "pay";
            }
            else
            {
                String msg = "下单失败";
                switch (responseVo.getCode())
                {
                    case 1:
                        msg += "防重令牌校验失败";
                        break;
                    case 2:
                        msg += "商品价格发生变化";
                        break;
                }

                attributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            if (e instanceof NoStockException){
                String msg = "下单失败，商品无库存";
                attributes.addFlashAttribute("msg", msg);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }

    }


}
