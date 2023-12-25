package com.oddity.geekmall.product.feign;

import com.oddity.common.to.SkuReductionTo;
import com.oddity.common.to.SpuBoundTo;
import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author oddity
 * @create 2023-07-15 21:51
 */

@FeignClient("geekmall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
