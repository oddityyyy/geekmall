package com.oddity.geekmall.order.dao;

import com.oddity.geekmall.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货原因
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:18:44
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
