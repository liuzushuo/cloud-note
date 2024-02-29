package com.cloudnote.note.api.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class NoteDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 编号
     */
    @TableId
    @JsonFormat(shape =JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String body;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 用户编号
     */
    private Integer userId;

    /**
     * 置顶
     */
    private Boolean top;

    /**
     * 状态【-1：彻底被移除，0：被移除，1：正常】
     */
    private Integer status;

    /**
     * 类型【1：笔记，2：小记】
     */
    private Integer type;

    /**
     * 是否收藏
     */
    private Boolean isCollect;

    /**
     * 所属收藏夹id
     */
    private Integer collectId;

}
