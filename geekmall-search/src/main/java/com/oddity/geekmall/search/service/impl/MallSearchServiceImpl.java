package com.oddity.geekmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.oddity.common.to.es.SkuEsModel;
import com.oddity.common.utils.R;
import com.oddity.geekmall.search.config.GeekmallElasticSearchConfig;
import com.oddity.geekmall.search.constant.EsConstant;
import com.oddity.geekmall.search.feign.ProductFeignService;
import com.oddity.geekmall.search.service.MallSearchService;
import com.oddity.geekmall.search.vo.AttrResponseVo;
import com.oddity.geekmall.search.vo.BrandVo;
import com.oddity.geekmall.search.vo.SearchParam;
import com.oddity.geekmall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oddity
 * @create 2023-09-27 22:37
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = null;

        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            SearchResponse response = client.search(searchRequest, GeekmallElasticSearchConfig.COMMON_OPTIONS);
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建结果数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();
        //1. 返回的所有查询到的商品
        SearchHits hits = response.getHits();
        ArrayList<SkuEsModel> skuEsModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                skuEsModels.add(esModel);
            }
        }
        result.setProducts(skuEsModels);

        //2. 所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()){
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = item.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        //3. 所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for(Terms.Bucket bucket : brand_agg.getBuckets()){
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = bucket.getKeyAsNumber().longValue();
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brandImg")).getBuckets().get(0).getKeyAsString();
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        //4. 所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //5. 页码
        result.setPageNum(param.getPageNum());
        //6. 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //7. 总页码
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);
        //8. 分页集合
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        //6. 构建面包屑导航
         //属性
        if (param.getAttrs() != null && param.getAttrs().size() > 0){
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                result.getAttrIds().add(Long.parseLong(s[0]));
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    String attrName = data.getAttrName();
                    navVo.setNavName(attrName);
                }else{
                    navVo.setNavName(s[0]);
                }
                String replace = repalceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.geekmall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }

         //品牌
        if (param.getBrandId() != null && param.getBrandId().size() > 0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0){
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {});
                StringBuffer stringBuffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brands){
                    stringBuffer.append(brandVo.getName());
                    replace = repalceQueryString(param, brandVo.getBrandId() + "" , "brandId"); //对于每一个brand把key和value消隐过后得到的最终请求参数，简介相当于清空品牌值
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.geekmall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        return result;
    }

    private String repalceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

    /**
     * 准备检索请求
     * @return
     * @param param
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        //查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        if (param.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        if (param.getBrandId() != null && param.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        if (param.getAttrs() != null && param.getAttrs().size() > 0){
            //attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String attrStr : param.getAttrs()){
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        if (param.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())){
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2){
                rangeQuery.gte(s[0]);
                rangeQuery.lte(s[1]);
            }else if (s.length == 1){
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        sourceBuilder.query(boolQueryBuilder);

        //排序
        if (!StringUtils.isEmpty(param.getSort())){
            //sort=hotScore_asc/desc
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort("skuPrice", order);
        }

        //分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        //聚合分析
         //品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brandImg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);
         //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("构建的DSL" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }
}
