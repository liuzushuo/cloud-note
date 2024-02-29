package com.cloudnote.collect.mq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.collect.service.ICollectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "collect_delete_topic", consumerGroup = "collect_delete_consumer_group")
public class CollectDeleteListener implements RocketMQListener<MessageExt> {
    @Autowired
    private ICollectService collectService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        JSONObject jsonObject = JSONUtil.parseObj(str);
        Integer collectId = jsonObject.getInt("collectId");
        Integer userId = jsonObject.getInt("userId");
        collectService.deleteFromCache(collectId, userId);
    }
}
