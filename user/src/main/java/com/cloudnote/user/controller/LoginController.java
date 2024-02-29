package com.cloudnote.user.controller;

import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.dto.LoginFormDTO;
import com.cloudnote.user.api.dto.RegisterFormDTO;
import com.cloudnote.user.service.ILoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private ILoginService loginService;
    @PostMapping("/login/email")
    public R<String> login(LoginFormDTO loginForm) {
        return loginService.login(loginForm);
    }

    @PostMapping("/register/email")
    public R<?> register(RegisterFormDTO registerForm) {
        return loginService.register(registerForm);
    }
}
