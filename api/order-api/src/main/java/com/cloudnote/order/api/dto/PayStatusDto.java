package com.cloudnote.order.api.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Mr.M
 * @version 1.0
 * @description 支付结果数据, 用于接收支付结果通知处理逻辑
 * @date 2022/10/4 16:49
 */
@Data
@ToString
public class PayStatusDto implements Serializable {
    private static final long serialVersionUID = 1L;

    //商户订单号
    String outTradeNo;
    //支付宝交易号
    String tradeNo;
    //交易状态
    String tradeStatus;
    //appid
    String appId;
    //total_amount
    String totalAmount;
}
