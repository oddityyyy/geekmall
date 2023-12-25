package com.oddity.geekmall.seckill.service;

import com.oddity.geekmall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-28 17:56
 */

public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
