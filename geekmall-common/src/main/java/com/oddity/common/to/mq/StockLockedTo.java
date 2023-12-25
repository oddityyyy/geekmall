package com.oddity.common.to.mq;

import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-24 17:08
 */

@Data
public class StockLockedTo {

    private Long id; //库存工作单的id

    private StockDetailTo detailTo; //工作单详情项的所有
}
