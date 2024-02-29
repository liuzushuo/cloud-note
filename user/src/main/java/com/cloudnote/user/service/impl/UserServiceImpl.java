package com.cloudnote.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.domain.User;
import com.cloudnote.common.api.dto.UserDTO;
import com.cloudnote.user.config.UserHolder;
import com.cloudnote.user.mapper.UserMapper;
import com.cloudnote.user.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public boolean emailIsExist(String email) {
        User user = getOne(Wrappers.lambdaQuery(User.class).eq(User::getEmail, email));
        return user!= null;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @Override
    public R getInfo() {
        Integer userId = UserHolder.getUser().getId();
        User user = getById(userId);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return R.ok(userDTO);
    }
}
