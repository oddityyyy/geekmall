package com.oddity.geekmall.product.dao;

import com.oddity.geekmall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 16:23:48
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
