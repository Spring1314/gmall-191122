package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 12:29
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RabbitService rabbitService;

    //保存支付信息到payment_info
    @Override
    public PaymentInfo savePaymentInfo(Long orderId, PaymentType alipay) {
        //1.根据orderId查询支付信息,防止二次支付
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("order_id", orderId));
        if (paymentInfo == null){
            //2.查询订单信息
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setOrderId(orderId);
            paymentInfo.setPaymentType(alipay.name());
            paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
            paymentInfo.setSubject(orderInfo.getTradeBody());
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
            paymentInfo.setCreateTime(new Date());
            //保存支付信息
            paymentInfoMapper.insert(paymentInfo);
        }

        return paymentInfo;
    }

    //更新支付信息表
    @Override
    public void paySuccess(Map<String, String> paramsMap) {
        //支付成功后，更新支付表信息,只修改支付状态时未支付的
        //商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号；

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>()
                .eq("out_trade_no", paramsMap.get("out_trade_no")));
        if (paymentInfo.getPaymentStatus().equals(OrderStatus.UNPAID.name())){
            //tradeNo
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            //callback_time
            paymentInfo.setCallbackTime(new Date());
            // callback_content
            paymentInfo.setCallbackContent(JSONObject.toJSONString(paramsMap));
            //payment_status
            paymentInfo.setPaymentStatus(OrderStatus.PAID.name());
            //1.修改支付表状态
            paymentInfoMapper.updateById(paymentInfo);

            //2.修改订单表的支付状态：未支付-》已支付 和 进度状态：未支付-》已支付
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    MqConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
        }
    }
}
