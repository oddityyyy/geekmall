package com.oddity.geekmall.ware;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GeekmallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeekmallWareApplication.class, args);
    }

}
