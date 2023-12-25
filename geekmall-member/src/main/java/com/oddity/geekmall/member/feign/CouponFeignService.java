package com.oddity.geekmall.member.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author oddity
 * @create 2023-06-25 16:34
 */
@FeignClient("oddity-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
