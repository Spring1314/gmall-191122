package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-27 18:53
 */
public interface OrderInfoService {
    boolean hasStock(Long skuId, Integer skuNum);

    Long saveOrder(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfo(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus paid, ProcessStatus paid1);

    void sendOrderStatus(Long orderId);

    //修改订单表的进度状态
    public void updateOrderStatus(Long orderId,ProcessStatus processStatus);

    public String initWareData(Long orderId);

    public Map initWareDate(OrderInfo orderInfo);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
