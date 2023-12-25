package com.oddity.geekmall.search.vo;

import com.oddity.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oddity
 * @create 2023-10-01 22:21
 */

@Data
public class SearchResult {

    private List<SkuEsModel> products;

    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    private List<CatalogVo> catalogs;
    private List<BrandVo> brands;
    private List<AttrVo> attrs;

    //面包屑导航
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
