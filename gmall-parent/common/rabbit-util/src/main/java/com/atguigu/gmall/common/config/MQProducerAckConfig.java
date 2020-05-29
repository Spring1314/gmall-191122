package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import javafx.scene.chart.ScatterChart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Administrator
 * @create 2020-05-29 18:16
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }
    //交换机消息确认
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
            log.info("消息发送成功" + JSONObject.toJSONString(correlationData));
        } else {
            log.info("消息发送失败" + cause + "数据：" + JSONObject.toJSONString(correlationData));
            //重新发送
            GmallCorrelationData gmallCorrelationData = (GmallCorrelationData)correlationData;
            trySendMessage(gmallCorrelationData);
        }
    }

    //重新发送消息
    private void trySendMessage(GmallCorrelationData gmallCorrelationData) {
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount < 2){
            gmallCorrelationData.setRetryCount(++retryCount);
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),
                    gmallCorrelationData.getMessage(),gmallCorrelationData);
            log.info("尝试重新发送次数" + retryCount);
        } else {
            log.info("重发次数用尽" + JSONObject.toJSONString(gmallCorrelationData));
        }
    }

    //队列消息确认
    //失败才会返回信息
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息体：" + message);
        System.out.println("应答码：" + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("交换机：" + exchange);
        System.out.println("路由键：" + routingKey);
        //失败重新发送
    }
}
