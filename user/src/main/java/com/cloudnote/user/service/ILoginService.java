package com.cloudnote.user.service;

import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.dto.LoginFormDTO;
import com.cloudnote.user.api.dto.RegisterFormDTO;

public interface ILoginService {
    /**
     * 登录
     * @param loginForm
     * @return
     */
    R login(LoginFormDTO loginForm);

    /**
     * 注册
     * @param registerForm
     * @return
     */
    R<?> register(RegisterFormDTO registerForm);

    /**
     * 注册用户
     * @param userId
     * @param password
     * @param email
     * @return
     */
    boolean registWithTx(Integer userId, String password, String email);

    /**
     * 退出登录
     * @param tokenKey
     */
    void logout(String tokenKey);
}
