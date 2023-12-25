package com.oddity.geekmall.coupon.service.impl;

import com.oddity.geekmall.coupon.dao.CouponSpuRelationDao;
import com.oddity.geekmall.coupon.entity.CouponSpuRelationEntity;
import com.oddity.geekmall.coupon.service.CouponSpuRelationService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("couponSpuRelationService")
public class CouponSpuRelationServiceImpl extends ServiceImpl<CouponSpuRelationDao, CouponSpuRelationEntity> implements CouponSpuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CouponSpuRelationEntity> page = this.page(
                new Query<CouponSpuRelationEntity>().getPage(params),
                new QueryWrapper<CouponSpuRelationEntity>()
        );

        return new PageUtils(page);
    }

}