package com.oddity.geekmall.ware.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-07-19 23:38
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;

    private Integer status;

    private String reason;
}
