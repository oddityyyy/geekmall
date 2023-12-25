package com.oddity.geekmall.coupon.dao;

import com.oddity.geekmall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 21:53:08
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
