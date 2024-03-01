package com.cloudnote.order.api.domain;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName(value = "tb_pay_record")
public class PayRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 本系统支付交易号
     */
    private Long payNo;

    /**
     * 第三方支付交易流水号
     */
    private String outPayNo;

    /**
     * 第三方支付渠道编号
     */
    private String outPayChannel;

    /**
     * 商品订单号
     */
    private Long orderId;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 订单总价单位元
     */
    private Float totalPrice;

    /**
     * 币种CNY
     */
    private String currency;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 支付成功时间
     */
    private Date paySuccessTime;

    /**
     * 用户id
     */
    private Integer userId;

}
