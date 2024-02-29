package com.cloudnote.user.api.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName("tb_user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    // 邮箱
    private String email;
    // 密码
    private String password;
    // 盐
    private String salt;
    // 头像地址
    private String headPic;
    // 等级（0：普通用户，1：会员）
    private Integer level;
    // 会员过期时间
    private Date endTime;
    // 注册时间
    private Date createTime;
}
