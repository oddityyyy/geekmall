package com.oddity.geekmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.coupon.entity.CouponEntity;

import java.util.Map;

/**
 * 优惠券信息
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 21:53:08
 */
public interface CouponService extends IService<CouponEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

