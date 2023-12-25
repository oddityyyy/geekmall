package com.oddity.geekmall.order.feign;

import com.oddity.geekmall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-20 20:45
 */

@FeignClient("geekmall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    public List<OrderItemVo> getCurrentUserCartItems();
}
