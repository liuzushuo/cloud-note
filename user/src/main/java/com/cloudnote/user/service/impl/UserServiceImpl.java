package com.cloudnote.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.api.dto.UserDTO;
import com.cloudnote.user.api.domain.User;
import com.cloudnote.user.config.UserHolder;
import com.cloudnote.user.mapper.UserMapper;
import com.cloudnote.user.service.IUserService;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private StringRedisTemplate redisTemplate;
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

    /**
     * 修改用户会员信息
     *
     * @param userId
     * @param level
     * @param endTime
     * @param tokenKey
     * @return
     */
    @Override
    public boolean updateUserVipInfo(Integer userId, Integer level, Date endTime,String tokenKey) {
        // 1.修改数据库
        boolean update = update(Wrappers.lambdaUpdate(User.class).set(User::getLevel, level).set(User::getEndTime, endTime).eq(User::getId, userId));
        // 2.更新缓存
        redisTemplate.opsForHash().put(tokenKey,"level", level.toString());
        redisTemplate.opsForHash().put(tokenKey,"endTime", DateUtil.format(endTime,"yyyy-MM-dd"));
        return update;
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param oldPwd
     * @param newPwd
     * @param tokenKey
     */
    @Override
    public boolean modifyPwd(Integer userId, String oldPwd, String newPwd,String tokenKey) {
        // 1. 查询用户信息
        User user = getById(userId);
        // 2.校验旧密码
        if (!user.getPassword().equals(new Sha256Hash(oldPwd, user.getSalt()).toHex())) {
            return false;
        }
        // 3.修改新密码
        user.setPassword(new Sha256Hash(newPwd, user.getSalt()).toHex());
        // 4.删除redis缓存的token
        redisTemplate.delete(tokenKey);
        return updateById(user);
    }

    /**
     * 修改头像
     *
     * @param userId
     * @param headPic
     * @param tokenKey
     * @return
     */
    @Override
    public boolean modifyBaseInfo(Integer userId, String headPic,String tokenKey) {
        // 1.修改头像
        boolean update = update(Wrappers.lambdaUpdate(User.class).set(User::getHeadPic, headPic).eq(User::getId, userId));
        // 2.修改缓存
        redisTemplate.opsForHash().put(tokenKey, "headPic", headPic);
        return true;
    }
}
