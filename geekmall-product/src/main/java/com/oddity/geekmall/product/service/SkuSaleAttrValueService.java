package com.oddity.geekmall.product.service;

import com.oddity.geekmall.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 16:23:48
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

