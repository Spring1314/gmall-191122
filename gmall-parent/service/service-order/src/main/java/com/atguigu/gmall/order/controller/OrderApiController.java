package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author Administrator
 * @create 2020-05-27 13:50
 * 订单管理
 */
@RestController
@RequestMapping("/api/order")
public class OrderApiController {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoService orderInfoService;
    @Value("${ware.url}")
    private String wareUrl;
    //1.生成交易号
    @GetMapping("/auth/tradeNo")
    public String tradeNo(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String tradeNo = UUID.randomUUID().toString();
        //保存到缓存
        String tradeNoKey = "user:" + userId + ":tradeCode";
        redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        return tradeNo;
    }

    //2.提交订单 submitOrder(order, tradeNo) /auth/submitOrder?tradeNo=' + tradeNo,
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,String tradeNo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String tradeNoKey = "user:" + userId + ":tradeCode";
        //1.判断交易号的正确性
        if (StringUtils.isEmpty(tradeNo)){
            return Result.fail().message("交易不合法");
        } else {
            //交易号存在，和缓存中的进行比对
            String cacheTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
            if (!cacheTradeNo.equals(tradeNo)){
                return Result.fail().message("交易不合法");
            } else {
                if (StringUtils.isEmpty(cacheTradeNo)){
                    return Result.fail().message("订单已提交，无需重复提交");
                }
            }
        }
        //交易号正确，删除交易号，防止订单二次提交（幂等性）
        redisTemplate.delete(tradeNoKey);

        //2.查询库存 http://www.gware.com/hasStock?skuId=10221&num=2
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean hasStock = orderInfoService.hasStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!hasStock){
                return Result.fail().message(orderDetail.getSkuName() + ":库存不足");
            }
        }

        //3.保存订单
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId  = orderInfoService.saveOrder(orderInfo);

        //4.删除购物车及缓存中已选中的商品

        return Result.ok(orderId);
    }
}
