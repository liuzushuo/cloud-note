package com.cloudnote.order.constants;

/**
 * 有关订单和支付的状态码
 */
public class StatusCode {
    /////////////////////////////////// 订单交易类型状态 ///////////////////////////////////
    public static final String ORDER_STATUS_UNPAID = "600001";  // 未付款
    public static final String ORDER_STATUS_PAID = "600002";    // 已付款
    public static final String ORDER_STATUS_CLOSED = "600003";  // 已关闭
    public static final String ORDER_STATUS_REFUND = "600004";  // 已退款
    public static final String ORDER_STATUS_COMPLETED = "600005";  // 已完成


    /////////////////////////////////// 支付记录交易状态 ///////////////////////////////////
    public static final String PAY_STATUS_UNPAID = "601001";    // 未付款
    public static final String PAY_STATUS_PAID = "601002";      // 已付款
    public static final String PAY_STATUS_REFUND = "601003";    // 已退款
}
