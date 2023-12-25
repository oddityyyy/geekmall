package com.oddity.geekmall.ware.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author oddity
 * @create 2023-10-21 16:20
 */

@FeignClient("geekmall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    public R addrInfo(@PathVariable("id") Long id);
}
