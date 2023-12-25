package com.oddity.geekmall.ware.listener;

import com.oddity.common.to.mq.OrderTo;
import com.oddity.common.to.mq.StockLockedTo;
import com.oddity.geekmall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author oddity
 * @create 2023-10-24 22:48
 */

@RabbitListener(queues = "stock.release.stock.queue")
@Service
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 1. 库存工作单存在：下订单成功，库存锁定成功，接下来的业务调用失败（例如扣减使用了的优惠信息等），导致订单回滚，但工作单还在（因为是远程调用）。直接自动解锁。
     * 2. 库存工作单不存在：远程调用锁库存失败，订单和工作单全部回滚，但是消息队列中的消息还在。
     * @param to 消息队列里面的实体
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        System.out.println("收到解锁库存的消息");
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {

        System.out.println("订单关闭准备解锁库存");
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
