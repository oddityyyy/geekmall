package com.oddity.geekmall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.oddity.common.utils.R;
import com.oddity.geekmall.product.entity.SkuImagesEntity;
import com.oddity.geekmall.product.entity.SpuInfoDescEntity;
import com.oddity.geekmall.product.feign.SeckillFeignService;
import com.oddity.geekmall.product.service.*;
import com.oddity.geekmall.product.vo.SeckillInfoVo;
import com.oddity.geekmall.product.vo.SkuItemSaleAttrVo;
import com.oddity.geekmall.product.vo.SkuItemVo;
import com.oddity.geekmall.product.vo.SpuItemAttrGroupVo;
import com.oddity.geekmall.product.dao.SkuInfoDao;
import com.oddity.geekmall.product.entity.SkuInfoEntity;
import com.oddity.geekmall.product.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SeckillFeignService seckillFeignService;

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)){
            wrapper.and((w) -> {
                w.ge("price", min);
            });
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    wrapper.and((w) -> {
                        w.le("price", max);
                    });
                }
            }catch (Exception e){

            }
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

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R seckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (seckillInfo.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = seckillInfo.getData(new TypeReference<SeckillInfoVo>() {}); //?
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        }, executor);

        CompletableFuture.allOf(saleAttrFuture, descAttrFuture, baseAttrFuture, imageFuture, seckillFuture).get();

        return skuItemVo;
    }
}