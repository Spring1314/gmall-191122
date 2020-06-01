package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 12:38
 * 支付宝接口实现
 */
@Service
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    //支付宝下单
    @Override
    public String submit(Long orderId) {
        //1.保存支付信息到payment_info
        PaymentInfo paymentInfo = paymentService.savePaymentInfo(orderId, PaymentType.ALIPAY);
        //2.统一收单下单并支付页面接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //四个必填信息out_trade_no:商户订单号
        // product_code:销售产品码，与支付宝签约的产品码名称。 注：目前仅支持FAST_INSTANT_TRADE_PAY
        //total_amount:订单总金额 subject:订单标题
        Map map = new HashMap<>();
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", paymentInfo.getTotalAmount());
        map.put("subject", paymentInfo.getSubject());
        request.setBizContent(JSONObject.toJSONString(map));
        request.setReturnUrl(AlipayConfig.return_payment_url);
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return response.getBody();
    }

    //退钱
    @Override
    public String refund(String outTradeNo){
        //根据outTradeNo查询支付表
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>()
                .eq("out_trade_no", outTradeNo));
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map map = new HashMap();
        map.put("out_trade_no",outTradeNo);
        //refund_amount 需要退款的金额
        map.put("refund_amount",paymentInfo.getTotalAmount());
        request.setBizContent(JSONObject.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return null;
    }
}
