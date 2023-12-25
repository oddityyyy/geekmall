package com.oddity.geekmall.cart.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author oddity
 * @create 2023-10-17 8:23
 */

@FeignClient("geekmall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R getSkuinfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/price")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
