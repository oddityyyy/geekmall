package com.oddity.geekmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:05:30
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);

}

