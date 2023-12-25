package com.oddity.geekmall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-11 17:10
 */

@Data
public class SkuItemSaleAttrVo {

    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
