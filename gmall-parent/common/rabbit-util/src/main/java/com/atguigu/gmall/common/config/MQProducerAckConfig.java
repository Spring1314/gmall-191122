package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-29 18:16
 */
@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
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
            //更新缓存中的数据
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),
                    JSONObject.toJSONString(gmallCorrelationData),5, TimeUnit.MINUTES);

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
        String key = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        if (StringUtils.isEmpty(key)){
            log.error("获取key失败");
            return ;
        }
        String target = (String) redisTemplate.opsForValue().get(key);
        GmallCorrelationData gmallCorrelationData = JSONObject.parseObject(target, GmallCorrelationData.class);
        if (gmallCorrelationData == null){
            log.error("GmallCorrelationData获取失败");
            return;
        }

        if (gmallCorrelationData.isDelay()){
            log.info("本次队列失败应答是正常的，不必重新发送消息");
            return;
        }
        trySendMessage(gmallCorrelationData);
    }
}
