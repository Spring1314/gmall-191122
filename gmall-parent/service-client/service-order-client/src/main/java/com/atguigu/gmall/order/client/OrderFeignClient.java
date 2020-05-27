package com.atguigu.gmall.order.client;

import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 * @create 2020-05-27 13:54
 */
@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {
    //生成交易号
    @GetMapping("api/order/auth/tradeNo")
    public String tradeNo();
}
