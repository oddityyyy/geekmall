package com.oddity.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-10-30 11:49
 */

@Data
public class SeckillOrderTo {

    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal num;
    private Long memberId;
}
