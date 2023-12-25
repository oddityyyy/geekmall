package com.oddity.geekmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-10-21 21:37
 */

@Data
public class OrderSubmitVo {

    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private String note;
}
