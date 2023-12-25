package com.oddity.geekmall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author oddity
 * @create 2023-07-20 0:36
 */

@EnableTransactionManagement
@MapperScan("com.oddity.geekmall.ware.dao")
@Configuration
public class WareMyBatisConfig {

    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
//        paginationInterceptor.setOverflow(true);
//        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
