package com.cloudnote.order.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Mr.M
 * @version 1.0
 * @description 创建商品订单
 * @date 2022/10/4 10:21
 */
@Data
@ToString
public class AddOrderDto implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 总价
     */
    private Float totalPrice;


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
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

}
