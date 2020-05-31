package com.atguigu.gmall.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.service.ListService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Administrator
 * @create 2020-05-30 11:50
 * 消息接收端
 */
@Component
public class MessageReceiver {

    @Autowired
    private ListService listService;
    //上架
    @RabbitListener(bindings = {@QueueBinding(
         value = @Queue(value = MqConst.QUEUE_GOODS_UPPER,autoDelete = "false",durable = "true"),
         exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
         key = MqConst.ROUTING_GOODS_UPPER
    )})
    public void upperGoods(Long skuId, Channel channel, Message message){
        try {
            listService.upperGoods(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            //重新发送
            if (message.getMessageProperties().isRedelivered()){
                //已经重新发送过了
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


    //下架
    @RabbitListener(bindings = {@QueueBinding(
          value = @Queue(value = MqConst.QUEUE_GOODS_LOWER,autoDelete = "false",durable = "true"),
          exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
          key = MqConst.ROUTING_GOODS_LOWER
    )})
    public void lowerGoods(Long skuId, Channel channel, Message message){

        try {
            listService.lowerGoods(skuId);
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
