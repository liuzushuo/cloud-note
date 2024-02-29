package com.cloudnote.user.controller;

import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private IUserService userService;
    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public R getInfo() {
        return userService.getInfo();
    }
}
