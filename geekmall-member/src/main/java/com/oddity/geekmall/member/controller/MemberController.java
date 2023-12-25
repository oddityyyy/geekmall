package com.oddity.geekmall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.oddity.common.exception.BizCodeEnume;
import com.oddity.geekmall.member.exception.PhoneExistException;
import com.oddity.geekmall.member.exception.UsernameExistException;
import com.oddity.geekmall.member.feign.CouponFeignService;
import com.oddity.geekmall.member.vo.MemberLoginVo;
import com.oddity.geekmall.member.vo.MemberRegistVo;
import com.oddity.geekmall.member.vo.SocialUser;
import com.oddity.geekmall.member.entity.MemberEntity;
import com.oddity.geekmall.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;



/**
 * 会员
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:05:30
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception {

        MemberEntity entity = memberService.login(socialUser);
        if (entity != null){
            //TODO 1. 登录成功后处理
            return R.ok().put("data", entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){

        MemberEntity entity = memberService.login(vo);
        if (entity != null){
            //TODO 1. 登录成功后处理
            return R.ok().put("data", entity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){

        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
