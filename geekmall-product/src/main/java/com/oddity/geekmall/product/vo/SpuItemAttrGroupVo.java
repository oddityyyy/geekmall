package com.oddity.geekmall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-11 17:04
 */

@Data
public class SpuItemAttrGroupVo {

    private String groupName;
    private List<Attr> attrs;
}
