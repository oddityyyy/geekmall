package com.oddity.geekmall.ware.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-22 14:15
 */

@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
