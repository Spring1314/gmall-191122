package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
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
        rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(),MqConst.DELAY_TIME * 10000);
        /*rabbitService.sendDelayedMessage(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,MqConst.ROUTING_ORDER_CANCEL,
                orderInfo.getId(),10000);*/
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

    //修改订单表的支付状态和进度状态
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(orderStatus.name());
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }

    //修改订单表的进度状态
    public void updateOrderStatus(Long orderId,ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }

    //扣减库存
    @Override
    public void sendOrderStatus(Long orderId) {
        //修改订单的进度状态
        this.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        //准备通知仓库那边所需的数据
        String result = initWareData(orderId);
        //发消息通知库存
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK,result);
    }

    //准备通知仓库那边所需的数据
    public String initWareData(Long orderId) {
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        Map map = initWareDate(orderInfo);
        return JSONObject.toJSONString(map);
    }

    public Map initWareDate(OrderInfo orderInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee",orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        //支付方式：  ‘1’ 为货到付款，‘2’为在线支付。
        map.put("paymentWay",orderInfo.getPaymentWay().equals("货到付款") ? "1" : "2");

        //wareId	 传入时的仓库编号
        map.put("wareId",orderInfo.getWareId());

        //购买商品明细 skuId,skuNum,skuName list<Map>
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        List<Map> listMap = orderDetailList.stream().map(orderDetail -> {
            Map skuMap = new HashMap();
            skuMap.put("skuId",orderDetail.getSkuId());
            skuMap.put("skuNum",orderDetail.getSkuNum());
            skuMap.put("skuName",orderDetail.getSkuName());
            return skuMap;
        }).collect(Collectors.toList());
        map.put("details",listMap);
        return map;
    }

    //拆单接口
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        //原始订单信息
        OrderInfo orderInfoOrigin = this.getOrderInfo(Long.parseLong(orderId));
        //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        List<Map> skuList = JSONObject.parseArray(wareSkuMap, Map.class);
        //子订单集合
        List<OrderInfo> orderInfoList = new ArrayList<>();
        for (Map map : skuList) {
            OrderInfo subOrderInfo = new OrderInfo();
            BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
            //DB自增长
            subOrderInfo.setId(null);
            subOrderInfo.setWareId(map.get("wareId").toString());
            //外键
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());

            //订单详情表
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            List<String> skuIdList = (List<String>) map.get("skuIds");
            List<OrderDetail> orderDetails = orderDetailList.stream().filter(orderDetail -> {
                for (String skuId : skuIdList) {
                    if (skuId.equals(orderDetail.getSkuId().toString())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            subOrderInfo.setOrderDetailList(orderDetails);
            orderInfoList.add(subOrderInfo);

            //保存订单
            saveOrderInfo(subOrderInfo);
        }
        //更新订单的状态
        this.updateOrderStatus(orderInfoOrigin.getId(),OrderStatus.SPLIT,ProcessStatus.SPLIT);
        return orderInfoList;
    }

    private void saveOrderInfo(OrderInfo subOrderInfo) {
        //保存订单表
        orderInfoMapper.insert(subOrderInfo);
        //保存订单详情表
        List<OrderDetail> orderDetailList = subOrderInfo.getOrderDetailList();
        orderDetailList.forEach(orderDetail -> {
            orderDetailMapper.insert(orderDetail);
        });
    }

}
