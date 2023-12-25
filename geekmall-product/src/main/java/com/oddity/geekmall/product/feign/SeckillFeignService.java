package com.oddity.geekmall.product.feign;

import com.oddity.common.utils.R;
import com.oddity.geekmall.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author oddity
 * @create 2023-10-29 18:40
 */

@FeignClient(value = "geekmall-seckill", fallback = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
