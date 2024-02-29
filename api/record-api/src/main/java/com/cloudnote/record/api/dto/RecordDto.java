package com.cloudnote.record.api.dto;

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
public class RecordDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 笔记id
     */
    private Long noteId;
    /**
     * 小记id
     */
    private Integer thingId;
    /**
     * 用户编号
     */
    private Integer userId;
    /**
     * 标题
     */
    private String title;
    /**
     * 最后一次修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 类型（1：笔记、2：小记）
     */
    private Integer type;
}
