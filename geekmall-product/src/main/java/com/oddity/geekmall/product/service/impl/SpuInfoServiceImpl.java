package com.oddity.geekmall.product.service.impl;

import com.oddity.common.constant.ProductConstant;
import com.oddity.common.to.SkuHasStockVo;
import com.oddity.common.to.SkuReductionTo;
import com.oddity.common.to.SpuBoundTo;
import com.oddity.common.to.es.SkuEsModel;
import com.oddity.common.utils.R;
import com.oddity.geekmall.product.entity.*;
import com.oddity.geekmall.product.feign.CouponFeignService;
import com.oddity.geekmall.product.feign.SearchFeignService;
import com.oddity.geekmall.product.feign.WareFeignService;
import com.oddity.geekmall.product.service.*;
import com.oddity.geekmall.product.vo.*;
import com.oddity.geekmall.product.dao.SpuInfoDao;
import com.oddity.geekmall.product.entity.*;
import com.oddity.geekmall.product.service.*;
import com.oddity.geekmall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.TypeReference;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1. 保存spu基本信息 `pms_spu_info`
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveSpuBaseInfo(infoEntity);
        
        //2. 保存spu的描述图片 `pms_spu_info_desc`
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3. 保存spu的图片集 `pms_spu_images`
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

        //4. 保存spu的规格参数 `pms_product_attr_value`
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((baseAttr) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(infoEntity.getId());
            productAttrValueEntity.setAttrId(baseAttr.getAttrId());

            String attrName = attrService.getById(baseAttr.getAttrId()).getAttrName();

            productAttrValueEntity.setAttrName(attrName);
            productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        //4.5 保存spu的积分信息 `sms_spu_bounds`
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        //5. 保存当前spu对应的所有sku信息
         //5.1 sku基本信息 `pms_sku_info`
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() != 0){
            skus.forEach((item) -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setSaleCount(0L);

                String defaultImg = "";
                for (Images img : item.getImages()){
                    if (img.getDefaultImg() == 1){
                        defaultImg = img.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //5.2 sku图片信息 `pms_sku_images`
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map((img) -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(img, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                }).filter((entity) -> {
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //5.3 sku销售属性 `pms_sku_sale_attr_value`
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = item.getAttr().stream().map((attr) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);

                //5.4 sku优惠、满减等信息 `geekmall_sms` -> `sms_sku_ladder` / `sms_sku_full_reduction` / `sms_member_price`
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);

                List<MemberPrice> memberPrice = item.getMemberPrice();
                List<com.oddity.common.to.MemberPrice> memberPriceList = memberPrice.stream().map((member) -> {
                    com.oddity.common.to.MemberPrice memberPriceTo = new com.oddity.common.to.MemberPrice();
                    memberPriceTo.setId(member.getId());
                    memberPriceTo.setName(member.getName());
                    memberPriceTo.setPrice(member.getPrice());
                    return memberPriceTo;
                }).collect(Collectors.toList());
                skuReductionTo.setMemberPrice(memberPriceList);

                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveSpuBaseInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.and((w) ->{
               w.eq("publish_status", status);
            });
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.and((w) -> {
               w.eq("brand_id", brandId);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.and((w) -> {
               w.eq("catalog_id", catelogId);
            });
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //每一个spuId都是统一的
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        HashSet<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attr> attrList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            R skusHasStock = wareFeignService.getSkusHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            List<SkuHasStockVo> data = skusHasStock.getData(typeReference);
            stockMap = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::isHasStock));
        }catch (Exception e){
            log.error("库存服务查询异常：原因{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;

        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();

            BeanUtils.copyProperties(sku, esModel);

            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //hasStock,hotScore
            //1. 发送远程调用ware库存系统查询是否有库存
            if (finalStockMap == null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //TODO 2. 热度评分，0.
            esModel.setHotScore(0L);

            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());

            //这里冗余设置一下
            esModel.setAttrs(attrList);

            return esModel;
        }).collect(Collectors.toList());

        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0){
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //TODO 重读调用？接口幂等性
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = getById(spuId);

        return spuInfoEntity;
    }
}