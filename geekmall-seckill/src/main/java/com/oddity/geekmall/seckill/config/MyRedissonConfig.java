package com.oddity.geekmall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author oddity
 * @create 2023-10-29 1:14
 */

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.1.150:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
