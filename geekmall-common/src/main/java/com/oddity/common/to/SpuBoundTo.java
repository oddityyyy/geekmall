package com.oddity.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-07-15 22:07
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
