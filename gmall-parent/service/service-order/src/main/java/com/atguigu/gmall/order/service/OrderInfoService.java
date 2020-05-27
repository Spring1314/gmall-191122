package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author Administrator
 * @create 2020-05-27 18:53
 */
public interface OrderInfoService {
    boolean hasStock(Long skuId, Integer skuNum);

    Long saveOrder(OrderInfo orderInfo);
}
