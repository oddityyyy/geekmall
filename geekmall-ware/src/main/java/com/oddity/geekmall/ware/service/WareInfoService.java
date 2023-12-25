package com.oddity.geekmall.ware.service;

import com.oddity.geekmall.ware.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:28:36
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}

