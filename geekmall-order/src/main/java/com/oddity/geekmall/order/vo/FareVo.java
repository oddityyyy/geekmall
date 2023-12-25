package com.oddity.geekmall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-10-22 0:08
 */

@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;
}
