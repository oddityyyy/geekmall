package com.oddity.geekmall.search.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-09 23:02
 */

@FeignClient("geekmall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    public R brandsInfo(@RequestParam List<Long> brandIds);
}
