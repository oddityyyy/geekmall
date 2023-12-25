package com.oddity.geekmall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author oddity
 * @create 2023-10-20 19:33
 */

public class OrderConfirmVo {

    @Setter @Getter
    List<MemberAddressVo> address;

    @Setter @Getter
    List<OrderItemVo> items;

    @Setter @Getter
    Integer integration;

    @Setter @Getter
    Map<Long, Boolean> stocks;

    //防重令牌
    @Setter @Getter
    String orderToken;

    public Integer getCount() {
        Integer i = 0;
        if (items != null && items.size() > 0){
            for(OrderItemVo item : items){
                i += item.getCount();
            }
        }
        return i;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null && items.size() > 0){
            for(OrderItemVo item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
