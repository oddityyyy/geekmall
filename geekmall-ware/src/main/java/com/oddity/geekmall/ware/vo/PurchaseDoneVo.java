package com.oddity.geekmall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author oddity
 * @create 2023-07-19 23:36
 */

@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;

    private List<PurchaseItemDoneVo> items;
}
