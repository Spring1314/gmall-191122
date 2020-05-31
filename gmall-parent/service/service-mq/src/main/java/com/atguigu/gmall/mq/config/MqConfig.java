package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-31 9:57
 * 基于插件的延迟消息
 */
@Configuration
public class MqConfig {
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    @Bean
    public Queue queue(){
        return QueueBuilder.durable(queue_delay_1).build();
    }

    @Bean
    public CustomExchange customExchange(){
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(customExchange()).with(routing_delay).noargs();
    }
}
