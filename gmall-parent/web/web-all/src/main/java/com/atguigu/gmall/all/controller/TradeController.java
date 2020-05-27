package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-27 11:56
 * 结算页面
 */
@Controller
public class TradeController {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;
    //去结算页面
    @GetMapping("/trade.html")
    public String toTradeHtml(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        //1.用户收货地址集合
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(Long.parseLong(userId));
        request.setAttribute("userAddressList",userAddressList);
        //2.选中商品集合
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(Long.parseLong(userId));
        List<OrderDetail> orderDetailList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cartInfo, orderDetail);
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            return orderDetail;
        }).collect(Collectors.toList());
        request.setAttribute("detailArrayList",orderDetailList);
        //3.商品总数量、金额
        long totalNum = orderDetailList.stream().collect(
                Collectors.summarizingInt(OrderDetail::getSkuNum)).getSum();
        request.setAttribute("totalNum",totalNum);

        double totalAmount = orderDetailList.stream().collect(Collectors.summarizingDouble(orderDetail -> {
            return orderDetail.getOrderPrice().doubleValue() * orderDetail.getSkuNum();
        })).getSum();
        request.setAttribute("totalAmount",totalAmount);
        //4.交易号
        String tradeNo = orderFeignClient.tradeNo();
        request.setAttribute("tradeNo",tradeNo);
        return "order/trade";
    }
}
