package com.oddity.geekmall.auth.feign;

import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author oddity
 * @create 2023-10-12 23:53
 */

@FeignClient("geekmall-third-party")
public interface ThirdPartFeignService {

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
