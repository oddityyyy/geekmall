package com.oddity.geekmall.auth.feign;

import com.oddity.common.utils.R;
import com.oddity.geekmall.auth.vo.SocialUser;
import com.oddity.geekmall.auth.vo.UserLoginVo;
import com.oddity.geekmall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author oddity
 * @create 2023-10-13 21:01
 */

@FeignClient("geekmall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
