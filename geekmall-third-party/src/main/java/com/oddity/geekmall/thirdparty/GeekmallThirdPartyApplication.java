package com.oddity.geekmall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GeekmallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeekmallThirdPartyApplication.class, args);
    }

}
