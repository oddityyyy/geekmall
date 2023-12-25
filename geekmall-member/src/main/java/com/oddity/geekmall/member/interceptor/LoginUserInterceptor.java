package com.oddity.geekmall.member.interceptor;

import com.oddity.common.constant.AuthServerConstant;
import com.oddity.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author oddity
 * @create 2023-10-20 16:51
 */

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if (match){
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null){
            loginUser.set(attribute);
            return true;
        }else {
            request.getSession().setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.oddity.com/login.html");
            return false;
        }
    }
}
