package com.atguigu.gmall.payment.service;

/**
 * @author Administrator
 * @create 2020-06-01 12:38
 * 支付宝
 */
public interface AlipayService {

    String submit(Long orderId);

    String refund(String outTradeNo);
}
