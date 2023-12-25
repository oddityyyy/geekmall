package com.oddity.geekmall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-09-27 22:32
 */

@Data
public class SearchParam {

    private String keyword;
    private Long catalog3Id;

    private String sort;

    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum = 1;

    private String _queryString;
}
