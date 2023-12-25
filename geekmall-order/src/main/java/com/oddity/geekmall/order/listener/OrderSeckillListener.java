package com.oddity.geekmall.order.listener;

import com.oddity.common.to.mq.SeckillOrderTo;
import com.oddity.geekmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author oddity
 * @create 2023-10-30 15:33
 */

@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrder, Channel channel, Message message) throws IOException {
        log.info("准备创建秒杀单的详细信息...");
        try {
            orderService.createSeckillOrder(seckillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
