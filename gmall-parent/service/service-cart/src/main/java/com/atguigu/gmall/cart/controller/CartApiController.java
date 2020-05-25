package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-25 18:24
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    //添加购物车
    @GetMapping("/addCart/{skuId}/{skuNum}")
    public CartInfo addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request){
        //用户id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //临时用户id
            userId = AuthContextHolder.getUserTempId(request);
        }
        return cartService.addCart(skuId,skuNum,userId);
    }
}
