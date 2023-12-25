package com.oddity.geekmall.seckill.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author oddity
 * @create 2023-10-29 0:32
 */

@FeignClient("oddity-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);
}
