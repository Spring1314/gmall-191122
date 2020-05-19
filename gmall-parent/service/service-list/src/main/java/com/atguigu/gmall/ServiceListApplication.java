package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Administrator
 * @create 2020-05-19 20:28
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class,
        scanBasePackages = {"com.atguigu.gmall"})
//scanBasePackages = {"com.atguigu.gmall"}) 和 @ComponentScan({"com.atguigu.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.atguigu.gmall"})
public class ServiceListApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class,args);
    }
}
