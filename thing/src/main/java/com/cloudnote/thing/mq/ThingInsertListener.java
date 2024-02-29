package com.cloudnote.thing.mq;

import com.cloudnote.thing.service.IThingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "thing_insert_topic", consumerGroup = "thing_insert_consumer_group")
public class ThingInsertListener implements RocketMQListener<MessageExt> {
    @Autowired
    private IThingService thingService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        Integer thingId = Integer.parseInt(str);
        thingService.addOrUpdateById(thingId);
    }
}
