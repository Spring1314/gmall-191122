package com.atguigu.gmall.cart.client.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-25 20:10
 */
@Component
public class CartDegradeFeignClient implements CartFeignClient {

    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum) {
        return null;
    }
}
