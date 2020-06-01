package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 12:28
 */
public interface PaymentService {
    PaymentInfo savePaymentInfo(Long orderId, PaymentType alipay);

    void paySuccess(Map<String, String> paramsMap);
}
