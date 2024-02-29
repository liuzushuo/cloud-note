package com.cloudnote.common.api.dto;

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
    private Date endTime;
}
