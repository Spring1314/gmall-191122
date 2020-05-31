package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.MqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @create 2020-05-29 16:51
 * 测试rabbit
 */
@RestController
public class RabbitController {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitService rabbitService;

    @GetMapping("/sendMessage")
    public Result sendMessage(){
        rabbitService.sendMessage("exchange1122","routingKey191122","191122");
        return Result.ok();
    }

    @GetMapping("/sendDelayedMessage")
    public Result sendDelayedMessage(){
        rabbitService.sendDelayedMessage(MqConfig.exchange_delay,MqConfig.routing_delay,
                "婷婷是我的呀",10000);
        return Result.ok();
    }
}
