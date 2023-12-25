package com.oddity.geekmall.ware.service;

import com.oddity.common.to.mq.OrderTo;
import com.oddity.common.to.mq.StockLockedTo;
import com.oddity.geekmall.ware.vo.SkuHasStockVo;
import com.oddity.geekmall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:28:35
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo orderTo);
}

