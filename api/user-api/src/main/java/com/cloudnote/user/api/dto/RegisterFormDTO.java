package com.cloudnote.user.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterFormDTO {
    private String email;
    // 验证码
    private String vc;
    // 验证码key
    private String vcKey;
}
