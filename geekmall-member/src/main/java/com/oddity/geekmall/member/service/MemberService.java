package com.oddity.geekmall.member.service;

import com.oddity.geekmall.member.exception.PhoneExistException;
import com.oddity.geekmall.member.exception.UsernameExistException;
import com.oddity.geekmall.member.vo.MemberLoginVo;
import com.oddity.geekmall.member.vo.MemberRegistVo;
import com.oddity.geekmall.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oddity.common.utils.PageUtils;
import com.oddity.geekmall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:05:30
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

