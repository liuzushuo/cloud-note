package com.cloudnote.assistant.config;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.cloudnote.common.api.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_KEY;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class AuthChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 连接前监听
     *
     * @param message
     * @param channel
     * @return
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        //1、判断是否首次连接
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            //2、判断token
            List<String> nativeHeader = accessor.getNativeHeader("Authorization");
            if (nativeHeader == null || nativeHeader.isEmpty()) {
                throw new RuntimeException("令牌不能为空");
            }
            String token = nativeHeader.get(0);
            if (StrUtil.isEmpty(token)) {
                throw new RuntimeException("令牌不能为空");
            }
            // 如果前端设置了令牌前缀，则裁剪掉前缀
            if (StrUtil.isNotEmpty(token) && token.startsWith("Bearer ")) {
                token = token.replaceFirst("Bearer ", "");
            }

            // 获取用户信息
            String tokenKey = LOGIN_USER_KEY + token;
            Map<Object, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);
            // 判断用户是否存在
            if (userMap.isEmpty()) {
                throw new RuntimeException("未登录");
            }
            // 将hash数据转为userDto
            UserDTO userDTO = BeanUtil.mapToBean(userMap, UserDTO.class, false);

            Principal principal = new Principal() {
                @Override
                public String getName() {
                    return String.valueOf(userDTO.getId());
                }
            };
            accessor.setUser(principal);

            return message;
        }
        //不是首次连接，已经登陆成功
        return message;
    }

}