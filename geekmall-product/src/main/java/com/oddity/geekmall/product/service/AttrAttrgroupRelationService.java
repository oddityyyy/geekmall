package com.oddity.geekmall.product.service;

import com.oddity.geekmall.product.vo.AttrGroupRelationVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 16:23:48
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelationVo> vos);
}

