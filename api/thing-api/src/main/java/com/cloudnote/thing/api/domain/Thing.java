package com.cloudnote.thing.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_thing")
public class Thing implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签
     */
    private String tags;

    /**
     * 内容
     */
    private String content;

    /**
     * 用户编号
     */
    private Integer userId;

    /**
     * 是否已完成【0：未完成、1：已完成】
     */
    private Boolean finished;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    /**
     * 最近修改的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 置顶（1：置顶，0：不置顶）
     */
    private Boolean top;

    /**
     * 状态【0：删除、-1：彻底删除、1：正常】
     */
    private Integer status;

    /**
     * 类别 【1：笔记、2：小记】
     */
    private Integer type;

}
