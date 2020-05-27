package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 * @create 2020-05-27 13:55
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public String tradeNo() {
        return null;
    }
}
