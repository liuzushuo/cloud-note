package com.cloudnote.thing.mq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cloudnote.thing.service.IThingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "thing_delete_topic", consumerGroup = "thing_delete_consumer_group")
public class ThingDeleteListener implements RocketMQListener<MessageExt> {
    @Autowired
    private IThingService thingService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        JSONObject jsonObject = JSONUtil.parseObj(str);
        Integer thingId = jsonObject.getInt("thingId");
        Integer userId = jsonObject.getInt("userId");
        thingService.deleteById(thingId, userId);
    }
}
