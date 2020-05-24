package com.atguigu.gmall.user;

import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Administrator
 * @create 2020-05-24 12:26
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.atguigu.gmall"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class,args);
    }
}
