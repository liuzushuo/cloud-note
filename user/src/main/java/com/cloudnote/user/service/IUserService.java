package com.cloudnote.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.domain.User;

import java.util.Date;

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

    /**
     * 修改用户会员信息
     * @param userId
     * @param level
     * @param endTime
     * @param tokenKey
     * @return
     */
    public boolean updateUserVipInfo(Integer userId, Integer level, Date endTime,String tokenKey);

    /**
     * 修改密码
     * @param userId
     * @param oldPwd
     * @param newPwd
     * @param tokenKey
     */
    boolean modifyPwd(Integer userId, String oldPwd, String newPwd,String tokenKey);

    /**
     *  修改头像
     * @param userId
     * @param headPic
     * @param tokenKey
     * @return
     */
    boolean modifyBaseInfo(Integer userId,String headPic,String tokenKey);
}
