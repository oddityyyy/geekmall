package com.oddity.geekmall.seckill.to;

import com.oddity.geekmall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author oddity
 * @create 2023-10-29 0:01
 */

@Data
public class SecKillSkuRedisTo {

    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //sku详细信息

    private SkuInfoVo skuInfo;


    //秒杀开始时间
    private Long startTime;


    //秒杀结束时间
    private Long endTime;


    //秒杀随机码
    private String randomCode;
}
