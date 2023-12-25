package com.oddity.geekmall.product.vo;

import com.oddity.geekmall.product.entity.SkuImagesEntity;
import com.oddity.geekmall.product.entity.SkuInfoEntity;
import com.oddity.geekmall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-11 14:42
 */

@Data
public class SkuItemVo {

    SkuInfoEntity info;

    List<SkuImagesEntity> images;

    List<SkuItemSaleAttrVo> saleAttr;

    SpuInfoDescEntity desp;

    List<SpuItemAttrGroupVo> groupAttrs;

    boolean hasStock = true;

    SeckillInfoVo seckillInfo;
}
