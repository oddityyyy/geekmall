package com.oddity.geekmall.order.feign;

import com.oddity.geekmall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-20 19:54
 */

@FeignClient("geekmall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    public List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
