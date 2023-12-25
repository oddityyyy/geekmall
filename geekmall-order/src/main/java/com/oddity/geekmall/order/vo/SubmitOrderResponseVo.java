package com.oddity.geekmall.order.vo;

import com.oddity.geekmall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author oddity
 * @create 2023-10-21 22:53
 */

@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;

    private Integer code; //0：成功 错误状态码
}
