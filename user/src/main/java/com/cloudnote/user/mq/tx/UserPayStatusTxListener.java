package com.cloudnote.user.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.user.service.IUserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 监听笔记收藏的事务
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_status_tx_topic", consumerGroup = "CN_USER_PAY_STATUS_TX_PRODUCER_GROUP")
public class UserPayStatusTxListener implements RocketMQListener<MessageExt> {
    @Autowired
    private IUserService userService;

    @SneakyThrows
    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        JSONObject params = JSONUtil.parseObj(str);
        Integer userId = params.getInt("userId");
        Date endTime = params.getDate("endTime");
        Boolean isSuccess = params.getBool("isSuccess");
        String tokenKey = params.getStr("tokenKey");
        if (isSuccess == true) {
            userService.updateUserVipInfo(userId, 1, endTime,tokenKey);
        }
    }
}
