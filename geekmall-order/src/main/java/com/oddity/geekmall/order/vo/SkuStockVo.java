package com.oddity.geekmall.order.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-21 15:38
 */

@Data
public class SkuStockVo {

    private Long skuId;

    private boolean hasStock;
}
