package com.cloudnote.collect.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.collect.service.ICollectService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听笔记收藏的事务
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "user_collect_tx_topic", consumerGroup = "default_collect_tx_consumer_group")
public class DefaultCollectTxListener implements RocketMQListener<MessageExt> {
    @Autowired
    private ICollectService collectService;

    @SneakyThrows
    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        JSONObject params = JSONUtil.parseObj(str);
        Integer userId = params.getInt("userId");
        // 创建默认收藏夹
        collectService.createDefaultCollect(userId);
    }
}
