package com.oddity.geekmall.product.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author oddity
 * @create 2023-08-03 19:48
 */

@FeignClient("geekmall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
