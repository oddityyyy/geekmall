package com.oddity.geekmall.order.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author oddity
 * @create 2023-10-22 1:35
 */

@FeignClient("geekmall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")
    public R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
