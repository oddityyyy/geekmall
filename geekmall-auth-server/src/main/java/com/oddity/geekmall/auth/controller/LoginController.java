package com.oddity.geekmall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.oddity.common.constant.AuthServerConstant;
import com.oddity.common.exception.BizCodeEnume;
import com.oddity.common.utils.R;
import com.oddity.common.vo.MemberRespVo;
import com.oddity.geekmall.auth.feign.MemberFeignService;
import com.oddity.geekmall.auth.feign.ThirdPartFeignService;
import com.oddity.geekmall.auth.vo.UserLoginVo;
import com.oddity.geekmall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author oddity
 * @create 2023-10-12 20:28
 */

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null){
            return "login";
        }else {
            return "redirect:http://geekmall.com";
        }
    }

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //1. 接口防刷

        //2. 验证码再次校验.redis
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000){
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = String.format("%06d", new Random().nextInt(1000000)) + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code.substring(0, 6));

        return R.ok();
    }

    //TODO 重定向携带数据，利用session原理。将数据放在session中。只要跳到下一个页面取出来这个数据之后，session里面的数据就会删掉
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){

        if (result.hasErrors()){

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.geekmall.com/reg.html";
        }

        //校验验证码
        String code = vo.getCode();

        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)){
            if (code.equals(s.split("_")[0])){
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                //调用远程服务进行注册
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0){
                    return "redirect:http://auth.geekmall.com/login.html";
                }else {
                    HashMap<String, Object> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.geekmall.com/reg.html";
                }
            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.geekmall.com/reg.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.geekmall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){

        R login = memberFeignService.login(vo);
        if (login.getCode() == 0){
            session.setAttribute(AuthServerConstant.LOGIN_USER, login.getData(new TypeReference<MemberRespVo>(){}));
            return "redirect:http://geekmall.com";
        }else{
            HashMap<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.geekmall.com/login.html";

        }
    }
}
