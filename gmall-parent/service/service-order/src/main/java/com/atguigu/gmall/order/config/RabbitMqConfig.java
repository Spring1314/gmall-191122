package com.atguigu.gmall.order.config;

import com.atguigu.gmall.common.constant.MqConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-31 10:27
 * MQ的配置类
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue queue(){
        return QueueBuilder.durable(MqConst.QUEUE_ORDER_CANCEL).build();
    }

    @Bean
    public CustomExchange customExchange(){
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(customExchange()).with(MqConst.ROUTING_ORDER_CANCEL).noargs();
    }
}
