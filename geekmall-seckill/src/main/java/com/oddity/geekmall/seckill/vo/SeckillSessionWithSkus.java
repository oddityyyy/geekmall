package com.oddity.geekmall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author oddity
 * @create 2023-10-28 21:19
 */

@Data
public class SeckillSessionWithSkus {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;



    private List<SeckillSkuVo> relationSkus;
}
