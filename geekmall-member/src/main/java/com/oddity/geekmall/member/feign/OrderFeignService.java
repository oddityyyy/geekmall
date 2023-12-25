package com.oddity.geekmall.member.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author oddity
 * @create 2023-10-26 22:48
 */

@FeignClient("oddity-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params);
}
