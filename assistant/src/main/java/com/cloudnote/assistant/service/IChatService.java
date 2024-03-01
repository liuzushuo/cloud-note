package com.cloudnote.assistant.service;


import com.cloudnote.assistant.api.dto.MessageDto;

import java.security.Principal;

public interface IChatService {
    /**
     * 向阿里云百炼发送消息
     * @param messageDto
     * @param principal
     */
    public void senMessage(MessageDto messageDto, Principal principal);
}
