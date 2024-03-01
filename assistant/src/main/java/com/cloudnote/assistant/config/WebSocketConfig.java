package com.cloudnote.assistant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private TaskScheduler messageBrokerTaskScheduler;
    @Autowired
    private AuthChannelInterceptor authChannelInterceptor;

    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    /**
     * 用于配置请求相关的参数
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("handshake")   // 连接请求的后缀 ws://localhost:8080/ws，默认是ws
                .setAllowedOrigins("*");  // 允许跨域
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor); // 握手前进行登录校验
    }

    /**
     * 用于将接收到的消息广播给订阅的用户
     *
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/socket")   // 全局配置，在订阅的地址前面要加/socket
                .setUserDestinationPrefix("/user")   // 配置用户目标的前缀，spring会将/user自动转换为用户id，进而实现私人订阅
                .enableSimpleBroker("/topic", "/queue")   // 配置消息代理，用于订阅/topic消息和/queue消息的节点
                .setHeartbeatValue(new long[]{10000, 20000})   // 设置心跳机制，当连接断开后，会通过心跳机制去检测连接是否存活，用于网络抖动导致的连接断开
                .setTaskScheduler(this.messageBrokerTaskScheduler);
    }
}
