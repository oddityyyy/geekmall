package com.oddity.geekmall.product.vo;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-07-08 16:59
 */
@Data
public class AttrRespVo extends AttrVo{

    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
