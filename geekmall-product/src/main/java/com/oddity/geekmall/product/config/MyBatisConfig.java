package com.oddity.geekmall.product.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author oddity
 * @create 2023-07-07 22:30
 */

@Configuration
@EnableTransactionManagement //开启事务
@MapperScan("com.oddity.geekmall.product.dao")
public class MyBatisConfig {
    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        paginationInterceptor.setOverflow(true);
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
