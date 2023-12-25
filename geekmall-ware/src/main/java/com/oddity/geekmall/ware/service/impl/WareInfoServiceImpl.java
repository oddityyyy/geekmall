package com.oddity.geekmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.oddity.common.utils.R;
import com.oddity.geekmall.ware.feign.MemberFeignService;
import com.oddity.geekmall.ware.vo.FareVo;
import com.oddity.geekmall.ware.vo.MemberAddressVo;
import com.oddity.geekmall.ware.dao.WareInfoDao;
import com.oddity.geekmall.ware.entity.WareInfoEntity;
import com.oddity.geekmall.ware.service.WareInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();

        R info = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if (data != null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal fare = new BigDecimal(substring);
            fareVo.setFare(fare);
            fareVo.setAddress(data);
            return fareVo;
        }
        return null;
    }

}