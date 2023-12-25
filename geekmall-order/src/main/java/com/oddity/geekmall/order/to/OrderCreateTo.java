package com.oddity.geekmall.order.to;

import com.oddity.geekmall.order.entity.OrderEntity;
import com.oddity.geekmall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-21 23:34
 */

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    //冗余
//    private BigDecimal payPrice;
//
//    private BigDecimal fare;
}
