package com.cloudnote.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.api.dto.UserDTO;
import com.cloudnote.common.util.TokenGenerator;
import com.cloudnote.user.api.domain.User;
import com.cloudnote.user.api.dto.LoginFormDTO;
import com.cloudnote.user.api.dto.RegisterFormDTO;
import com.cloudnote.user.service.ILoginService;
import com.cloudnote.user.service.IUserService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cloudnote.common.constants.MqConstants.USER_COLLECT_TX_GROUP;
import static com.cloudnote.common.constants.MqConstants.USER_COLLECT_TX_TOPIC;
import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_KEY;
import static com.cloudnote.common.constants.RedisConstants.LOGIN_USER_TTL;

@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IUserService userService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 登录
     *
     * @param loginForm
     * @return
     */
    @Override
    public R login(LoginFormDTO loginForm) {
        // 1.参数校验
        if (StrUtil.isBlank(loginForm.getEmail())) {
            return R.fail("邮箱不能为空");
        }
        if (StrUtil.isBlank(loginForm.getPassword())) {
            return R.fail("密码不能为空");
        }
        // 2.根据邮箱查询用户
        User user = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getEmail, loginForm.getEmail()));
        if (user == null) {
            return R.fail("邮箱不存在");
        }
        // 3.校验密码
        if (!user.getPassword().equals(new Sha256Hash(loginForm.getPassword(), user.getSalt()).toHex())) {
            return R.fail("密码错误");
        }
        // 4.生成token
        String token = TokenGenerator.generateValue(user.getId().toString());
        String tokenKey = LOGIN_USER_KEY + token;
        // 5.将登陆用户放入缓存
        UserDTO userDto = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> map = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((filedName, filedValue) -> {
                    if (filedValue != null) {
                        return filedValue.toString();
                    }
                    return null;
                }));
        redisTemplate.opsForHash().putAll(tokenKey, map);
        // 设置过期时间
        redisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 6.返回
        Map<String, Object> rspMap = new HashMap<String, Object>();
        rspMap.put("access_token", token);
        rspMap.put("expires_in", TimeUnit.MINUTES.toMillis(LOGIN_USER_TTL));
        return R.ok(rspMap);
    }

    /**
     * 注册
     *
     * @param registerForm
     * @return
     */
    @Transactional
    @Override
    public R<?> register(RegisterFormDTO registerForm) {
        // 1.校验参数
        String email = registerForm.getEmail();
        if (StrUtil.isBlank(email)) {
            return R.fail("邮箱不能为空");
        }
        if (StrUtil.isBlank(registerForm.getVc())) {
            return R.fail("验证码不能为空");
        }
        // 2.校验验证码
        String vc = redisTemplate.opsForValue().get(registerForm.getVcKey());
        if (StrUtil.isBlank(vc)) {
            return R.fail("验证码已过期");
        }
        if (!vc.equals(registerForm.getVc())) {
            return R.fail("验证码错误");
        }
        // 3.注册
        User user = new User();
        String salt = RandomUtil.randomString(20);
        String password = RandomUtil.randomString(6);
        user.setEmail(email);
        user.setSalt(salt);
        user.setPassword(new Sha256Hash(password, salt).toHex());
        userService.save(user);

        // 4.发消息
        JSONObject params = new JSONObject();
        params.put("userId", user.getId());
        params.put("password", password);
        params.put("email", user.getEmail());
        Message<String> message = MessageBuilder.withPayload(params.toString()).build();
        rocketMQTemplate.sendMessageInTransaction(USER_COLLECT_TX_GROUP, USER_COLLECT_TX_TOPIC, message, null);
        return R.ok();
    }

    /**
     * 注册用户
     * @param userId
     * @param password
     * @param email
     * @return
     */
    @Override
    public boolean registWithTx(Integer userId, String password, String email) {
        // 1.查询用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            // 注册失败
            return false;
        }
        // 2.发送邮件，通知用户账号的初始密码
        String content = "<p>【云笔记】尊敬的" + email + ":</p>" +
                "<p>您已成功注册云笔记账号，其初始密码为：<b style='font-size:20px;color:blue;'>" + password + "</b>。</p>" +
                "<p>请尽量快速登陆账号，修改其初始密码</p>";
        try {
            MailUtil.send(email, "云账号注册通知", content, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
