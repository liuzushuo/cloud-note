package com.cloudnote.collect.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_collect")
public class Collect implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏笔记数量
     */
    private Integer count;

    /**
     * 用户编号
     */
    private Integer userId;

    /**
     * 是否为默认收藏夹
     */
    private Boolean isDefault;
}
