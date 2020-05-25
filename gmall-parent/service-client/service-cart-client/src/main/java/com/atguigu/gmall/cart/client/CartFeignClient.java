package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @create 2020-05-25 20:09
 */
@FeignClient(value = "service-cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {
    //添加购物车
    @GetMapping("api/cart/addCart/{skuId}/{skuNum}")
    public CartInfo addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request);
}
