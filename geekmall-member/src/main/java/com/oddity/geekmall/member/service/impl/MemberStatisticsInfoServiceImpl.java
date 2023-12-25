package com.oddity.geekmall.member.service.impl;

import com.oddity.geekmall.member.dao.MemberStatisticsInfoDao;
import com.oddity.geekmall.member.entity.MemberStatisticsInfoEntity;
import com.oddity.geekmall.member.service.MemberStatisticsInfoService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("memberStatisticsInfoService")
public class MemberStatisticsInfoServiceImpl extends ServiceImpl<MemberStatisticsInfoDao, MemberStatisticsInfoEntity> implements MemberStatisticsInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberStatisticsInfoEntity> page = this.page(
                new Query<MemberStatisticsInfoEntity>().getPage(params),
                new QueryWrapper<MemberStatisticsInfoEntity>()
        );

        return new PageUtils(page);
    }

}