package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author Administrator
 * @create 2020-05-25 18:30
 */
public interface CartService {

    CartInfo addCart(Long skuId, Integer skuNum, String userId);
}
