package com.oddity.common.to;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-08-03 18:04
 */

@Data
public class SkuHasStockVo {

    private Long skuId;

    private boolean hasStock;
}
