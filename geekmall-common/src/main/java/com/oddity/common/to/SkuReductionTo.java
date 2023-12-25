package com.oddity.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author oddity
 * @create 2023-07-15 22:34
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;

    private List<MemberPrice> memberPrice;
}
