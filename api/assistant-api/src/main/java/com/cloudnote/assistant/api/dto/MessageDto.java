package com.cloudnote.assistant.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MessageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private String content;
    /**
     * 角色：user（用户）或assistent（大模型）
     */
    private String role;
    /**
     * 消息发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date chatAt;
}
