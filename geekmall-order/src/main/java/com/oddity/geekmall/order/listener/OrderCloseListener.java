package com.oddity.geekmall.order.listener;

import com.oddity.geekmall.order.entity.OrderEntity;
import com.oddity.geekmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author oddity
 * @create 2023-10-25 1:09
 */

@RabbitListener(queues = "order.release.order.queue")
@Service
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单" + entity.getOrderSn());
        try {
            orderService.closeOrder(entity);
            //TODO 手动调用支付宝收单，保证一旦收单，支付宝立即不能付款退出，防止支付了订单却不存在
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
