package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 * @create 2020-05-31 13:10
 * 支付页面
 */
@Controller
public class PaymentController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    //payment.gmall.com/pay.html?orderId
    @GetMapping("/pay.html")
    public String toPay(Long orderId, Model model){
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }
}
