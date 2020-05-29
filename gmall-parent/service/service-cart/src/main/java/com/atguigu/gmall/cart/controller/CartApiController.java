package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request){
        //用户id
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            //临时用户id
            userId = AuthContextHolder.getUserTempId(request);
        }
        return cartService.addToCart(skuId,skuNum,userId);
    }

    //去购物车结算页面，获得用户的购物车集合
    @GetMapping("/cartList")
    public Result cartList(HttpServletRequest request) {
        //获得真实用户id
        String userId = AuthContextHolder.getUserId(request);
        //获得临时用户id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList =  cartService.cartList(userId, userTempId);
        return Result.ok(cartInfoList);
    }

    //更改商品选中状态
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("isChecked") Integer isChecked,
                            HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.checkCart(skuId,isChecked,userId);
        return Result.ok();
    }

    //获得选中的商品集合
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId){
        return cartService.getCartCheckedList(userId);
    }

}
