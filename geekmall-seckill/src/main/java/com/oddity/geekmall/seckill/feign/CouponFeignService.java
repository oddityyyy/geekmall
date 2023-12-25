package com.oddity.geekmall.seckill.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author oddity
 * @create 2023-10-28 18:03
 */

@FeignClient("oddity-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/late3DaySession")
    public R getLate3DaySession();
}
