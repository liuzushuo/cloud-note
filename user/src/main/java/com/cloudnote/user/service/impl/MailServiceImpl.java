package com.cloudnote.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.service.IMailService;
import com.cloudnote.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MailServiceImpl implements IMailService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IUserService userService;
    /**
     * 发送邮箱验证码
     *
     * @param email
     * @return 验证码存入redis的key
     */
    @Override
    public R<String> getEmailRegisterVC(String email) {
        // 校验参数
        if (StrUtil.isBlank(email)){
            return R.fail("邮箱不能为空");
        }
        // 1.判断该邮箱是否已被注册
        boolean isExist = userService.emailIsExist(email);
        if (isExist) {
            return R.fail("该邮箱已被注册");
        }
        // 2.发送验证码
        String code = RandomUtil.randomString(6);       // 数字+字母

        Long effectiveTime = 15L;
        String content = "<p>【云笔记】尊敬的" + email + ":</p>" +
                "<p>您正在申请注册账号注册服务，如本人操作，请勿泄露该验证码！</p>" +
                "<p>验证码为:<b style='font-size:20px;color:blue;'>" + code + "</b>" +
                "<p>有效时间为 " + effectiveTime + " 分钟</p>";

        try {
            MailUtil.send(email, "云笔记注册验证码", content, true);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("验证码发送失败");
        }
        // 3.将验证码存入redis
        String eravcTokenKey = "eravcToken:" + email + ":" + IdUtil.randomUUID();
        try {
            redisTemplate.opsForValue().set(eravcTokenKey, code, effectiveTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("验证码存储失败");
        }
        return R.ok(eravcTokenKey,"发送成功");
    }
}
