package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-29 19:11
 */
@Service
public class RabbitService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    public void sendMessage(String exchange, String routingKey, Object msg){
        GmallCorrelationData correlationData = new GmallCorrelationData();
        String id = UUID.randomUUID().toString().replace("-","");
        correlationData.setId(id);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);
        correlationData.setMessage(msg);
        //保存GmallCorrelationData一份到缓存，防止队列接受消息失败时无法获得该对象
        redisTemplate.opsForValue().set(id, JSONObject.toJSONString(correlationData),5, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend(exchange,routingKey,msg,correlationData);
    }

    //发送延迟消息
    public void sendDelayedMessage(String exchange, String routingKey,Object msg, int delayTime) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String id = UUID.randomUUID().toString().replace("-","");
        gmallCorrelationData.setId(id);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);
        gmallCorrelationData.setDelayTime(delayTime);
        gmallCorrelationData.setDelay(true);
        //保存一份到缓存，防止队列接受消息失败无法获得
        redisTemplate.opsForValue().set(id,JSONObject.toJSONString(gmallCorrelationData),
                5,TimeUnit.MINUTES);
       rabbitTemplate.convertAndSend(exchange,routingKey,msg,message -> {
           message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime());
           System.out.println("消息发送时间：" + new Date());
           return message;
       },gmallCorrelationData);
    }
}
