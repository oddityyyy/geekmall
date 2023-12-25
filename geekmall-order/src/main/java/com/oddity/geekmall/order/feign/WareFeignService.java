package com.oddity.geekmall.order.feign;

import com.oddity.common.utils.R;
import com.oddity.geekmall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-21 15:33
 */

@FeignClient("geekmall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
