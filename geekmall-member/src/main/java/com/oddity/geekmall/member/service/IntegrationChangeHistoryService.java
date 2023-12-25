package com.oddity.geekmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:05:30
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

