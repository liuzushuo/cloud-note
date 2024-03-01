package com.cloudnote.order.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.order.api.domain.Orders;
import com.cloudnote.order.api.domain.PayRecord;
import com.cloudnote.order.service.IOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static com.cloudnote.order.constants.StatusCode.*;


@Slf4j
@Component
@RocketMQTransactionListener(txProducerGroup = "CN_PAY_STATUS_SAVE_TX_PRODUCER_GROUP")
public class PayStatusSaveTxListener implements RocketMQLocalTransactionListener {
    @Autowired
    private IOrdersService ordersService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject params = JSONUtil.parseObj(jsonString);
        String payNo = params.getStr("payNo");
        Long orderId = params.getLong("orderId");
        String tradeNo = params.getStr("tradeNo");
        Boolean isSuccess = params.getBool("isSuccess");
        try {
            ordersService.saveAliPayStatusWithTx(payNo, orderId, tradeNo,isSuccess);
        } catch (Exception e) {
            e.printStackTrace();
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject params = JSONUtil.parseObj(jsonString);
        String payNo = params.getStr("payNo");
        Long orderId = params.getLong("orderId");
        PayRecord payRecord = ordersService.getPayRecordByPayno(payNo);
        Orders order = ordersService.getById(orderId);
        if (payRecord.getStatus().equals(PAY_STATUS_PAID) && order.getStatus().equals(ORDER_STATUS_PAID)) {
            return RocketMQLocalTransactionState.COMMIT;
        } else if (payRecord.getStatus().equals(PAY_STATUS_UNPAID)) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
