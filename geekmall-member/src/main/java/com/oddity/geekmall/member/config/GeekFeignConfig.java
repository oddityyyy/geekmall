package com.oddity.geekmall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author oddity
 * @create 2023-10-20 22:07
 */

@Configuration
public class GeekFeignConfig {

    //Feign远程调用丢失请求头问题
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1. RequestContextHolder 拿到刚进来的请求，从spring上下文中
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes.getRequest(); //老请求
                if (request != null){
                    //同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    //给新请求同步老请求的Cookie
                    requestTemplate.header("Cookie", cookie);
                }
            }
        };
    }
}
