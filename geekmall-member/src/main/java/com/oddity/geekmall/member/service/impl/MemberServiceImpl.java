package com.oddity.geekmall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oddity.common.utils.HttpUtils;
import com.oddity.geekmall.member.dao.MemberLevelDao;
import com.oddity.geekmall.member.entity.MemberLevelEntity;
import com.oddity.geekmall.member.exception.PhoneExistException;
import com.oddity.geekmall.member.exception.UsernameExistException;
import com.oddity.geekmall.member.vo.MemberLoginVo;
import com.oddity.geekmall.member.vo.MemberRegistVo;
import com.oddity.geekmall.member.vo.SocialUser;
import com.oddity.geekmall.member.dao.MemberDao;
import com.oddity.geekmall.member.entity.MemberEntity;
import com.oddity.geekmall.member.service.MemberService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {

        MemberEntity entity = new MemberEntity();
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        entity.setNickname(vo.getUserName());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        this.baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", password));
        if (entity == null){
            return null;
        }else {
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches){
                return entity;
            }else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {

        //登录和注册合并逻辑
        String uid = socialUser.getUid();
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null){
            //用户已注册，更新token和过期时间
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            memberDao.updateById(update);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        }else {
            MemberEntity regist = new MemberEntity();
            try {
                HashMap<String, String> query = new HashMap<String, String>();
                query.put("access_token", socialUser.getAccess_token());
                HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200){
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    regist.setNickname(name);
                    regist.setGender(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }
}