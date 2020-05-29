package com.atguigu.gmall.common.service;

import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Administrator
 * @create 2020-05-29 19:11
 */
@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object msg){
        GmallCorrelationData correlationData = new GmallCorrelationData();
        String id = UUID.randomUUID().toString().replace("-","");
        correlationData.setId(id);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);
        correlationData.setMessage(msg);
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,correlationData);
    }
}
