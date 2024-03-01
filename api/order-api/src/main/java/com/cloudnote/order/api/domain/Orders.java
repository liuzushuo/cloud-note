package com.cloudnote.order.api.domain;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 *  实体类。
 *
 * @author 42456
 * @since 2023-12-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_order")
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @TableId
    private Long id;

    /**
     * 总价
     */
    private Float totalPrice;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 交易状态
     */
    private String status;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 订单描述
     */
    private String orderDescrip;

    /**
     * 购买年限
     */
    private Integer amount;

    /**
     * 会员到期时间
     */
    private Date endTime;


}
