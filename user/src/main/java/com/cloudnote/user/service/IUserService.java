package com.cloudnote.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.domain.User;

public interface IUserService extends IService<User> {
    /**
     * 判断邮箱是否已注册
     * @param email
     * @return
     */
    boolean emailIsExist(String email);

    /**
     * 获取用户信息
     * @return
     */
    R getInfo();
}
