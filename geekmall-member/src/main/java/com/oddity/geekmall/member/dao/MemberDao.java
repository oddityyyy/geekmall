package com.oddity.geekmall.member.dao;

import com.oddity.geekmall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:05:30
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
