package com.oddity.geekmall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GeekmallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeekmallCouponApplication.class, args);
    }

}
