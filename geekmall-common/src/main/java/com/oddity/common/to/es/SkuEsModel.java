package com.oddity.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author oddity
 * @create 2023-08-02 21:41
 */

@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;
    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attr> attrs;

    @Data
    public static class Attr{

        private Long attrId;

        private String attrName;

        private String attrValue;
    }
}
