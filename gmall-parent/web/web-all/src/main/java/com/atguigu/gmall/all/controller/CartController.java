package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * @author Administrator
 * @create 2020-05-25 20:04
 * 购物车管理
 */
@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;
    //添加购物车页面
    @GetMapping("/addCart.html")
    public String addCart(Long skuId,Integer skuNum,Model model){
        CartInfo cartInfo = cartFeignClient.addToCart(skuId, skuNum);
        model.addAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }

    //去购物车结算页面
    @GetMapping("/cart.html")
    public String toCart(){
        return "cart/index";
    }

}
