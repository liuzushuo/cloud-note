package com.cloudnote.collect.mq.tx;

import cn.hutool.core.util.BooleanUtil;
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
 * 监听笔记收藏的事务
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "note_collect_tx_topic", consumerGroup = "collect_change_tx_consumer_group")
public class CollectChangeTxListener implements RocketMQListener<MessageExt> {
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
        Integer oldCollectId = params.getInt("oldCollectId");
        Boolean oldCollectStatus = BooleanUtil.isTrue(params.getBool("oldCollectStatus"));
        // 修改收藏夹
        Collect collect = Collect.builder().id(collectId).userId(userId).count(count).build();
        collectService.updateCollect(collect);

        // 如果是收藏操作，并且原收藏夹id不等于目标收藏夹id，并且原本已经处于收藏状态（简单来说就是笔记更换收藏夹），则需要将原收藏夹count值减一
        if (isCollect && oldCollectStatus && !oldCollectId.equals(collectId)) {
            collect = Collect.builder().id(oldCollectId).userId(userId).count(-1).build();
            collectService.updateCollect(collect);
        }
    }
}
