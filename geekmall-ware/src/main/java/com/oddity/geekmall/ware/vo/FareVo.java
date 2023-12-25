package com.oddity.geekmall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-10-21 16:59
 */

@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;
}
