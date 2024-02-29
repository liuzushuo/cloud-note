package com.cloudnote.user.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.cloudnote.common.api.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_KEY;
import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_TTL;


public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate redisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        token = token.split(" ")[1];
        // 获取用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);
        // 判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        // 将hash数据转为userDto
        UserDTO userDTO = BeanUtil.mapToBean(userMap, UserDTO.class, false);
        // 存在，将用户信息存入ThreadLocal
        UserHolder.saveUser(userDTO);
        // 刷新token有效期
        redisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户信息
        UserHolder.removeUser();
    }
}
