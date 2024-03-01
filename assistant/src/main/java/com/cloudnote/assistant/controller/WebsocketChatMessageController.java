package com.cloudnote.assistant.controller;


import com.cloudnote.assistant.api.dto.MessageDto;
import com.cloudnote.assistant.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class WebsocketChatMessageController {
    @Autowired
    private IChatService chatService;

    /**
     * 对话接口
     *
     * @param messageDto 用户发送的消息
     * @param principal  缓存了用户id
     */
    @MessageMapping("/chatMessage/send")
    public void chat(@Payload MessageDto messageDto, Principal principal) {
        chatService.senMessage(messageDto, principal);
    }
}
