package com.cloudnote.user.service;


import com.cloudnote.common.api.dto.R;

public interface IMailService {
    /**
     * 发送邮箱验证码
     *
     * @param email
     * @return 验证码存入redis的key
     */
    R<String> getEmailRegisterVC(String email);
}
