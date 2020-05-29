package com.atguigu.gmall.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Administrator
 * @create 2020-05-29 16:17
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@ComponentScan({"com.atguigu.gmall"})
public class ServiceMqApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMqApplication.class,args);
    }
}
