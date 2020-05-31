package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClient;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Administrator
 * @create 2020-05-27 18:54
 */
@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Value("${ware.url}")
    private String wareUrl;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;

    //2.查询库存 http://localhost:9001
    @Override
    public boolean hasStock(Long skuId, Integer skuNum) {
        //模拟发送一条http请求 http://www.gware.com/hasStock?skuId=10221&num=2
        String result = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return ("1").equals(result);
    }

    //3.保存订单
    @Override
    public Long saveOrder(OrderInfo orderInfo) {
        //3.1保存order_info
        //总金额 total_amount
        orderInfo.sumTotalAmount();
        //订单状态 order_status
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //订单交易编号 out_trade_no
        String outTradeNo = "ATGUIGU" + UUID.randomUUID().toString().replace("-","");
        orderInfo.setOutTradeNo(outTradeNo);
        //订单描述 trade_body
        String tradeBody = "";
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            tradeBody += orderDetail.getSkuName();
            orderInfo.setTradeBody(tradeBody);
        }
        //创建时间 create_time
        orderInfo.setCreateTime(new Date());
        //process_status 进度状态
        orderInfo.setProcessStatus(ProcessStatus.WAITING_DELEVER.name());
        orderInfoMapper.insert(orderInfo);

        //3.2保存order_detail
        orderDetailList.forEach(orderDetail -> {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
            /*//4.删除购物车及缓存中已选中的商品
            //4.1删除购物车中选中的商品
            QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",orderInfo.getUserId()).eq("sku_id",orderDetail.getSkuId());
            cartInfoMapper.delete(wrapper);

            //4.2删除缓存中已选中的商品
            Long userId = orderInfo.getUserId();
            String cacheKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
            redisTemplate.delete(cacheKey);*/

        });
        //5.超时未支付订单，自动取消订单
        /*rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(),MqConst.DELAY_TIME * 10000);*/
        rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(),10000);
        return orderInfo.getId();
    }

    //取消订单
    @Override
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.name())){
            System.out.println(orderId);
            orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
            orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());
            orderInfoMapper.updateById(orderInfo);
        }
    }

    //根据订单id获得订单信息
    @Override
    public OrderInfo getOrderInfo(Long orderId) {

        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }
}
