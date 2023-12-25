package com.oddity.geekmall.ware.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author oddity
 * @create 2023-10-24 20:25
 */

@FeignClient("geekmall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
