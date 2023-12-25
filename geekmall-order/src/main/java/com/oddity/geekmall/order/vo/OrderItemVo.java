package com.oddity.geekmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author oddity
 * @create 2023-10-20 19:41
 */

@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
}
