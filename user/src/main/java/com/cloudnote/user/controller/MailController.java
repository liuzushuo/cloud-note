package com.cloudnote.user.controller;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.user.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class MailController {
    @Autowired
    private IMailService mailService;

    /**
     * 发送注册邮箱验证码
     *
     * @param email
     * @return 查询验证码的redis的key
     */
    @GetMapping("/register/vc")
    public R<?> getEmailRegisterAccountVC(String email) {
        return mailService.getEmailRegisterVC(email);
    }

}
