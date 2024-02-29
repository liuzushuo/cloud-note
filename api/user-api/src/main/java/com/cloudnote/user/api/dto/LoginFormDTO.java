package com.cloudnote.user.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginFormDTO {
    private String email;
    private String password;
}
