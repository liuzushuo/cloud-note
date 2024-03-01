package com.cloudnote.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.order.api.domain.Orders;
import com.cloudnote.order.api.domain.PayRecord;
import com.cloudnote.order.api.dto.AddOrderDto;
import com.cloudnote.order.api.dto.PayRecordDto;
import com.cloudnote.order.api.dto.PayStatusDto;
import com.cloudnote.order.config.AlipayConfig;
import com.cloudnote.order.constants.StatusCode;
import com.cloudnote.order.mapper.IOrdersMapper;
import com.cloudnote.order.mapper.IPayRecordMapper;
import com.cloudnote.order.service.IOrdersService;
import com.cloudnote.order.util.QRCodeUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

import static com.cloudnote.common.constants.MqConstants.*;


/**
 * 服务层实现。
 *
 * @author 42456
 * @since 2023-12-15
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<IOrdersMapper, Orders> implements IOrdersService {
    @Autowired
    private IPayRecordMapper payRecordMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private OrdersServiceImpl currentProxy;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    /**
     * @param userId
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @description 创建商品订单
     * @author Mr.M
     * @date 2022/10/4 11:02
     */
    @Transactional
    @Override
    public PayRecordDto createOrder(Integer userId, AddOrderDto addOrderDto) {
        // 1.创建订单
        Orders orders = saveOrders(userId, addOrderDto);
        // 2.创建支付记录
        PayRecord payRecord = createPayRecord(orders);
        Long payNo = payRecord.getPayNo();

        // 3.生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        // 支付二维码的url
        String url = String.format(qrcodeurl, payNo);
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            return null;
        }

        // 4.构造返回值
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.PayRecord
     * @description 查询支付交易记录
     * @author Mr.M
     * @date 2022/10/20 23:38
     */
    @Override
    public PayRecord getPayRecordByPayno(String payNo) {
        PayRecord payRecord = payRecordMapper.selectOne(Wrappers.lambdaQuery(PayRecord.class).eq(PayRecord::getPayNo, payNo));
        return payRecord;
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param userId 用户id
     * @param payNo  支付记录id
     * @param tokenKey
     * @return 支付记录信息
     */
    @Override
    public PayRecordDto queryPayResult(Integer userId, String payNo,String tokenKey) {
        // 1.查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        if (payStatusDto == null) {
            // 说明用户没有扫码支付
            PayRecordDto payRecordDto = new PayRecordDto();
            payRecordDto.setStatus(StatusCode.PAY_STATUS_UNPAID);
            return payRecordDto;
        }
        // 2.当支付成功后，更新支付记录表的支付状态和订单表的订单状态为支付成功
        currentProxy.saveAliPayStatus(payStatusDto,tokenKey);
        // 3.返回支付记录
        PayRecord payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付交易号
     * @return 支付结果
     */
    @Override
    public PayStatusDto queryPayResultFromAlipay(String payNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.URL,
                APP_ID,
                APP_PRIVATE_KEY,
                AlipayConfig.FORMAT,
                AlipayConfig.CHARSET,
                ALIPAY_PUBLIC_KEY,
                AlipayConfig.SIGNTYPE
        );
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("请求支付宝查询支付结果异常");
        }
        if (!response.isSuccess() && !response.getCode().equals("40004")) {
            throw new RuntimeException("请求支付宝查询支付结果失败");
        }
        if (!response.isSuccess() && response.getCode().equals("40004")) {
            // 用户没有扫码支付，直接返回
            return null;
        }
        String body = response.getBody();
        // 封装支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        // 解析支付结果
        String tradeNo = response.getTradeNo();
        String tradeStatus = response.getTradeStatus();
        String totalAmount = response.getTotalAmount();
        payStatusDto.setOutTradeNo(payNo);
        payStatusDto.setTradeNo(tradeNo); // 支付宝的交易号
        payStatusDto.setTradeStatus(tradeStatus); // 交易状态
        payStatusDto.setAppId(APP_ID);
        payStatusDto.setTotalAmount(totalAmount); // 总金额

        return payStatusDto;
    }

    /**
     * @param payStatusDto 支付结果信息
     * @param tokenKey
     * @return void
     * @description 保存支付宝支付结果
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    @Transactional
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto,String tokenKey) {
        String payNo = payStatusDto.getOutTradeNo();
        PayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }
        Long orderId = payRecord.getOrderId();
        Orders order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        String statusFromDb = payRecord.getStatus();
        if (statusFromDb.equals(StatusCode.PAY_STATUS_PAID)) {
            // 说明订单已支付，无需再更新
            return;
        }

        // TODO:发送消息
        JSONObject params = new JSONObject();
        params.put("payNo", payNo);
        params.put("orderId", orderId);
        params.put("tradeNo", payStatusDto.getTradeNo());
        params.put("userId",order.getUserId());
        params.put("endTime",order.getEndTime());
        params.put("isSuccess",payStatusDto.getTradeStatus().equals("TRADE_SUCCESS"));
        params.put("tokenKey",tokenKey);
        Message<String> message = MessageBuilder.withPayload(params.toString()).build();
        rocketMQTemplate.sendMessageInTransaction(PAY_STATUS_SAVE_TX_GROUP, PAY_STATUS_SAVE_TX_TOPIC, message, null);
    }

    /**
     * 保存支付结果
     *
     * @param payNo
     * @param orderId
     * @param tradeNo
     * @param isSuccess
     */
    @Override
    public void saveAliPayStatusWithTx(String payNo, Long orderId, String tradeNo,Boolean isSuccess) {
        // 查询支付记录和订单
        PayRecord payRecord = getPayRecordByPayno(payNo);
        Orders order = getById(orderId);
        // 判断交易状态
        if (isSuccess) {
            // 如果支付成功
            // 更新支付记录表为支付成功
            payRecord.setStatus(StatusCode.PAY_STATUS_PAID);
            // 补全信息
            // 支付宝订单号
            payRecord.setOutPayNo(tradeNo);
            // 第三方支付渠道编号
            payRecord.setOutPayChannel("Alipay");
            // 支付成功的时间
            payRecord.setPaySuccessTime(new Date());
            int update = 0;
            try {
                update = payRecordMapper.updateById(payRecord);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("更新支付记录异常");
            }
            if (update < 1) {
                throw new RuntimeException("更新支付记录失败");
            }
            // 更新订单表为支付成功
            order.setStatus(StatusCode.ORDER_STATUS_PAID);
            boolean result = false;
            try {
                result = updateById(order);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("更新订单状态异常");
            }
            if (!result) {
                throw new RuntimeException("更新订单状态失败");
            }
        }
    }

    /**
     * 保存支付记录
     *
     * @param orders
     * @return
     */
    public PayRecord createPayRecord(Orders orders) {
        //订单ID
        Long orderId = orders.getId();
        // 1.查询订单
        Orders ordersDb = getById(orderId);
        // 如果此订单不存在，则不能添加支付记录
        if (ordersDb == null) {
            throw new RuntimeException("订单不存在");
        }
        // 2.判断订单状态，如果已支付则抛异常
        // 如果此订单支付结果为成功，也不再添加支付记录，避免重复支付
        if (ordersDb.getStatus().equals(StatusCode.PAY_STATUS_PAID)) {
            throw new RuntimeException("此订单已支付");
        }

        // 3.添加支付记录
        PayRecord payRecord = new PayRecord();
        //生成支付交易流水号
        long payNo = IdUtil.getSnowflakeNextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orderId);//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(new Date());
        payRecord.setStatus(StatusCode.PAY_STATUS_UNPAID);//未支付
        payRecord.setUserId(orders.getUserId());
        int insert = payRecordMapper.insert(payRecord);
        if (insert <= 0) {
            throw new RuntimeException("添加支付记录失败");
        }
        return payRecord;

    }

    /**
     * 保存订单信息
     *
     * @param userId
     * @param addOrderDto
     * @return
     */
    public Orders saveOrders(Integer userId, AddOrderDto addOrderDto) {
        // 插入订单表
        Orders order = new Orders();
        //生成订单号
        long orderId = IdUtil.getSnowflakeNextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(new Date());
        order.setStatus(StatusCode.ORDER_STATUS_UNPAID);//未支付
        order.setUserId(userId);
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setAmount(addOrderDto.getAmount());
        order.setEndTime(addOrderDto.getEndTime());
        boolean insert = save(order);
        if (!insert) {
            throw new RuntimeException("创建订单失败");
        }

        return order;
    }
}
