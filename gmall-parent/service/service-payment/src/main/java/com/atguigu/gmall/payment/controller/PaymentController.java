package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-06-01 11:45
 * 支付宝支付管理
 */
@Controller
@RequestMapping("/api/payment/alipay")
public class PaymentController {

    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;

    //http://api.gmall.com/api/payment/alipay/submit/{orderId}
    //支付宝下单
    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String submit(@PathVariable("orderId") Long orderId){
        return alipayService.submit(orderId);
    }

    //重定向至支付成功页面 通知买家付款成功 http://api.gmall.com/api/payment/alipay/callback/return
    @GetMapping("/callback/return")
    public String callback(){
        return "redirect:" + AlipayConfig.return_order_url;
    }

    //异步通知商家支付成功 /api/payment/alipay/callback/notify
    @ResponseBody
    @PostMapping("/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> paramsMap) throws Exception {
        //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key,
                    AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        System.out.println(paramsMap);
        //gmt_create=2020-06-01 19:36:12, charset=utf-8, gmt_payment=2020-06-01 19:36:36,
        // notify_time=2020-06-01 19:36:37,
        // subject=Apple iPhone 11 (A2223) 64GB 黑色 移动联通电信4G手机 双卡双待Apple iPhone 11 (A2223) 128GB 黑色 移动联通电信4G手机 双卡双待,
        // buyer_id=2088102181094242, invoice_amount=28995.00, version=1.0,
        // notify_id=2020060100222193636094240509049938,
        // fund_bill_list=[{"amount":"28995.00","fundChannel":"ALIPAYACCOUNT"}], n
        // otify_type=trade_status_sync, out_trade_no=ATGUIGU86940ccba8a34bb1ba1d90d2ca128469,
        // total_amount=28995.00, trade_status=TRADE_SUCCESS, trade_no=2020060122001494240512015364,
        // auth_app_id=2016102400749150, receipt_amount=28995.00, point_amount=0.00,
        // app_id=2016102400749150, buyer_pay_amount=28995.00, seller_id=2088102180845670}
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，
            //  校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            if ("TRADE_SUCCESS".equals(paramsMap.get("trade_status"))){
                paymentService.paySuccess(paramsMap);
            } else {
                return "failure";
            }
            return "success";
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
    }

    //退钱
    @GetMapping("/refund/{outTradeNo}")
    @ResponseBody
    public String refund(@PathVariable("outTradeNo") String outTradeNo ){
        return alipayService.refund(outTradeNo);
    }
}
