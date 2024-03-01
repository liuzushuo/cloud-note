package com.cloudnote.user.controller;

import cn.hutool.core.util.StrUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.api.domain.User;
import com.cloudnote.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_KEY;

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

    /**
     * 修改用户基本信息
     */
    @PutMapping("/baseInfo")
    public R modifyBaseInfo(User user, HttpServletRequest request) {
        // 获取token
        String token = request.getHeader("authorization");
        token = token.split(" ")[1];
        // 获取用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        String headPic = user.getHeadPic();
        if (StrUtil.isEmpty(headPic)) {
            return R.fail("头像不能为空");
        }
        Integer userId = user.getId();
        if (userId == null) {
            return R.fail("用户编号错误");
        }
        boolean update = userService.modifyBaseInfo(userId, headPic, tokenKey);
        return update ? R.ok("更新成功") : R.fail("更新失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/modifyPwd")
    public R modifyPwd(Integer userId, String oldPwd, String newPwd, HttpServletRequest request) {
        // 获取token
        String token = request.getHeader("authorization");
        token = token.split(" ")[1];
        // 获取用户信息
        String tokenKey = LOGIN_USER_KEY + token;
        if (StrUtil.isEmpty(oldPwd)) {
            return R.fail("原始密码为空");
        }
        if (StrUtil.isEmpty(newPwd)) {
            return R.fail("新密码为空");
        }
        if (newPwd.length() < 6 || newPwd.length() > 16) {
            return R.fail("密码长度必须在6-16位之间");
        }
        boolean result = userService.modifyPwd(userId, oldPwd, newPwd, tokenKey);
        return result ? R.ok("修改成功") : R.fail("修改失败");
    }
}
