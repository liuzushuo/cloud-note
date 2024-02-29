package com.cloudnote.collect.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.collect.service.ICollectService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听笔记删除或恢复的事务
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "note_restore_or_delete_tx_topic", consumerGroup = "collect_update_tx_consumer_group")
public class CollectUpdateTxListener implements RocketMQListener<MessageExt> {
    @Autowired
    private ICollectService collectService;

    @SneakyThrows
    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        JSONObject params = JSONUtil.parseObj(str);
        Integer userId = params.getInt("userId");
        Integer collectId = params.getInt("collectId");
        Boolean isCollect = params.getBool("isCollect");
        Integer count = params.getInt("count");
        if (isCollect) {
            Collect collect = Collect.builder().id(collectId).userId(userId).count(count).build();
            collectService.updateCollect(collect);
        }
    }
}
