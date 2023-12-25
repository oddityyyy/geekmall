package com.oddity.geekmall.coupon.dao;

import com.oddity.geekmall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 21:53:08
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
