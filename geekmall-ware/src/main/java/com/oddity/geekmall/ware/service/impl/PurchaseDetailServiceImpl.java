package com.oddity.geekmall.ware.service.impl;

import com.oddity.geekmall.ware.dao.PurchaseDetailDao;
import com.oddity.geekmall.ware.entity.PurchaseDetailEntity;
import com.oddity.geekmall.ware.service.PurchaseDetailService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().eq("purchase_id", key);
            });
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status", status);
        }

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> purchaseDetailEntities = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return purchaseDetailEntities;
    }

}