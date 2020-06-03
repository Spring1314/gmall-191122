package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-31 10:33
 * 消息接收端
 */
@Component
public class MessageReceiver {
    @Autowired
    private OrderInfoService orderInfoService;

    //超时未支付取消订单
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void delayedMessageReceiver(Long orderId, Channel channel, Message message){
        try {
            orderInfoService.cancelOrder(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            if (message.getMessageProperties().isRedelivered()){
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,autoDelete = "false",durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = MqConst.ROUTING_PAYMENT_PAY
    )})
    public void updateOrderStatus(Long orderId, Channel channel,Message message){
        try {
            //修改订单表的支付状态和进度状态
            orderInfoService.updateOrderStatus(orderId, OrderStatus.PAID, ProcessStatus.PAID);
            //扣减库存
            orderInfoService.sendOrderStatus(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            if (message.getMessageProperties().isRedelivered()){
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,autoDelete = "false", durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = MqConst.ROUTING_WARE_ORDER
    )})
    //接收商品减库结果消息
    public void updateWareDataAndOrderStatus(String msg, Channel channel, Message message){
        try {
            System.out.println(msg);
            Map map = JSONObject.parseObject(msg, Map.class);
            //orderId	订单系统的订单ID
            //status	状态： ‘DEDUCTED’  (已减库存)/‘OUT_OF_STOCK’  (库存超卖)
            if ("DEDUCTED".equals(map.get("status"))){
                //扣减库存成功，修改订单的支付状态和进度状态
                orderInfoService.updateOrderStatus(Long.parseLong((String)map.get("orderId")),
                        OrderStatus.WAITING_DELEVER,ProcessStatus.WAITING_DELEVER);
            } else {
                //扣减库存失败，修改订单的进度状态
                orderInfoService.updateOrderStatus(Long.parseLong((String)map.get("orderId")),
                        ProcessStatus.STOCK_EXCEPTION);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            if (message.getMessageProperties().isRedelivered()){
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
