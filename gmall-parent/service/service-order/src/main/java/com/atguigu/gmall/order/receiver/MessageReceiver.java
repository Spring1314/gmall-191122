package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Administrator
 * @create 2020-05-31 10:33
 * 消息接收端
 */
@Component
public class MessageReceiver {
    @Autowired
    private OrderInfoService orderInfoService;

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
}
