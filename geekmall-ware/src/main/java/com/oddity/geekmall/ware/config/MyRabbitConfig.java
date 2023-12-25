package com.oddity.geekmall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @author oddity
 * @create 2023-10-24 10:17
 */

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct //MyRabbit对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        //服务收到消息回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据（消息的唯一id）
             * @param b 消息是否收到
             * @param s 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                System.out.println("confirm...correlationData[" + correlationData + "]==>ack[" + b + "]==>cause[" + s + "]");
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                System.out.println("Fail Message[" + returnedMessage.getMessage() +
                        "]==>replyCode[" + returnedMessage.getReplyCode() +
                        "]==>replyText["+ returnedMessage.getReplyText() +
                        "]==>exchange[" + returnedMessage.getExchange() +
                        "]==>routingKey[" + returnedMessage.getRoutingKey() + "]");
            }
        });
    }

    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue(){
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release.#", null);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "order.seckill.order", null);
    }
}

/**
 * 自定义消息转换器，由默认的jdk序列化变为json序列化
 */
@Configuration
class messageConverterConfig{

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
