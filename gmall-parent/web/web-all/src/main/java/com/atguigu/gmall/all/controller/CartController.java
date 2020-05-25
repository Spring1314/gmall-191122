package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-25 20:04
 * 购物车管理
 */
@Controller
public class CartController {
    @Autowired
    private CartFeignClient cartFeignClient;
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name="user_id") Long userId,
                          @RequestParam(name="sku_num") Integer skuNum, HttpServletRequest request, Model model){
        CartInfo cartInfo = cartFeignClient.addCart(userId, skuNum, request);
        model.addAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }
}
