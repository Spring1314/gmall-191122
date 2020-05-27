package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Administrator
 * @create 2020-05-25 20:09
 */
@FeignClient(value = "service-cart",fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {
    //添加购物车
    //注意HttpServletRequest没有实现序列化接口，远程调用的参数必须实现序列化接口
    @GetMapping("api/cart/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum);
}
