package com.cloudnote.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
@Data
public class UserDTO {
    private Integer id;
    // 邮箱
    private String email;
    // 头像地址
    private String headPic;
    // 等级（0：普通用户，1：会员）
    private Integer level;
    // 会员过期时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime;
}
