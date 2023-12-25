package com.oddity.geekmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-07-19 11:49
 */

@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;
}
