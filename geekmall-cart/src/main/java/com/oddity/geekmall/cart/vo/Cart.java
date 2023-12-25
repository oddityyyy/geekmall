package com.oddity.geekmall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author oddity
 * @create 2023-10-14 19:56
 */

public class Cart {

    List<CartItem> items;

    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0){
            for (CartItem item : items){
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0){
            for (CartItem item : items){
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (items != null && items.size() > 0){
            for (CartItem item : items){
                if (item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
