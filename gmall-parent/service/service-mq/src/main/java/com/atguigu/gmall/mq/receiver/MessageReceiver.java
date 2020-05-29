package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Administrator
 * @create 2020-05-29 19:17
 * 接收消息
 */
@Component
public class MessageReceiver {

    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(name = "queue1122",autoDelete = "false",durable = "true"),
            exchange = @Exchange(name = "exchange1122"),
            key = {"routingKey1122"}
    )})
    public void messageReceiver(String msg, Channel channel, Message message){

        try {
            int i = 1 / 0;
            System.out.println(msg);
            //参数一：指定是哪一个消息  参数二：是否删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            //e.printStackTrace();
            //发送失败，是否重新发送
            if(message.getMessageProperties().isRedelivered()){
                //已经重新发送过了
                try {
                    System.out.println("已经重新发送过了，没机会了");
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                //第一次重新发送
                try {
                    System.out.println("有一次重新发送的机会哦");
                    //第二个参数：是否批量执行 第三个参数：是否重新消费
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
