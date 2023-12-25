package com.oddity.geekmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.oddity.geekmall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.oddity.geekmall.product.dao")
@SpringBootApplication
public class GeekmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeekmallProductApplication.class, args);
    }

}
