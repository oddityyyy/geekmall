package com.oddity.geekmall.member.service.impl;

import com.oddity.geekmall.member.dao.MemberCollectSubjectDao;
import com.oddity.geekmall.member.entity.MemberCollectSubjectEntity;
import com.oddity.geekmall.member.service.MemberCollectSubjectService;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("memberCollectSubjectService")
public class MemberCollectSubjectServiceImpl extends ServiceImpl<MemberCollectSubjectDao, MemberCollectSubjectEntity> implements MemberCollectSubjectService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberCollectSubjectEntity> page = this.page(
                new Query<MemberCollectSubjectEntity>().getPage(params),
                new QueryWrapper<MemberCollectSubjectEntity>()
        );

        return new PageUtils(page);
    }

}