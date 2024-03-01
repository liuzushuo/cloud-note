package com.cloudnote.search.config;

import com.cloudnote.common.api.dto.UserDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断ThreadLocal中是否有用户信息
        UserDTO user = UserHolder.getUser();
        if (user == null){
            // 拦截
            response.setStatus(401);
            return false;
        }
        // 放行
        return true;
    }
}
