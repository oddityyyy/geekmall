package com.oddity.geekmall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.oddity.common.utils.HttpUtils;
import com.oddity.common.utils.R;
import com.oddity.geekmall.auth.feign.MemberFeignService;
import com.oddity.common.vo.MemberRespVo;
import com.oddity.geekmall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * @author oddity
 * @create 2023-10-14 22:19
 */

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {

        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "4ad4b14ccefe7900c61f8502b78a153de8054108ad2de66c6fad072c848c8312");
        map.put("client_secret", "172178fdb4498df87e7ca13348d0ba868c867b77fd5709b7017100c02177cace");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.geekmall.com/oauth2.0/gitee/success");
        map.put("code", code);
        //1. 根据code换取accessToken
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), new HashMap<>(), map);
        if (response.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            HashMap<String, String> query = new HashMap<String, String>();
            query.put("access_token", socialUser.getAccess_token());
            HttpResponse responseDetail = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), query);
            if (responseDetail.getStatusLine().getStatusCode() == 200){
                String jsonDetail = EntityUtils.toString(responseDetail.getEntity());
                JSONObject jsonObject = JSON.parseObject(jsonDetail);
                String uid = String.valueOf(jsonObject.get("id"));
                socialUser.setUid(uid);
            }

            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode() == 0){

                MemberRespVo data = oauthlogin.getData(new TypeReference<MemberRespVo>() {});
                log.info("登陆成功：用户：{}", data.toString());
                session.setAttribute("loginUser", data);

                return "redirect:http://geekmall.com";
            }else {
                return "redirect:http://auth.geekmall.com/login.html";
            }
        }else {
            return "redirect:http://auth.geekmall.com/login.html";
        }
    }
}
