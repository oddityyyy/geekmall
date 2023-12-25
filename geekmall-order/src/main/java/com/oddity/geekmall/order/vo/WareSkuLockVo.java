package com.oddity.geekmall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-22 14:10
 */

@Data
public class WareSkuLockVo {

    private String orderSn;

    private List<OrderItemVo> locks;
}
