package com.cloudnote.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.order.api.domain.Orders;
import com.cloudnote.order.api.domain.PayRecord;
import com.cloudnote.order.api.dto.AddOrderDto;
import com.cloudnote.order.api.dto.PayRecordDto;
import com.cloudnote.order.api.dto.PayStatusDto;

/**
 * 订单相关的接口
 */
public interface IOrdersService extends IService<Orders> {
    /**
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @description 创建商品订单
     * @author Mr.M
     * @date 2022/10/4 11:02
     */
    public PayRecordDto createOrder(Integer userId, AddOrderDto addOrderDto);

    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 查询支付交易记录
     * @author Mr.M
     * @date 2022/10/20 23:38
     */
    public PayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     *
     * @param userId 用户id
     * @param payNo 支付记录id
     * @param tokenKey
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(Integer userId, String payNo,String tokenKey);

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo);

    /**
     * @param payStatusDto 支付结果信息
     * @param tokenKey
     * @return void
     * @description 保存支付宝支付结果
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto,String tokenKey);

    /**
     * 保存支付结果
     * @param payNo
     * @param orderId
     * @param tradeNo
     * @param isSuccess
     */
    public void saveAliPayStatusWithTx(String payNo,Long orderId,String tradeNo,Boolean isSuccess);
    /**
     * TODO:发送通知结果
     *
     * @param message
     */
//    public void notifyPayResult(MqMessage message);
}
