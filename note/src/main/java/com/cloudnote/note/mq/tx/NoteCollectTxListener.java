package com.cloudnote.note.mq.tx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.note.service.INoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RocketMQTransactionListener(txProducerGroup = "CN_NOTE_COLLECT_TX_PRODUCER_GROUP")
public class NoteCollectTxListener implements RocketMQLocalTransactionListener {
    @Autowired
    private INoteService noteService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        String jsonString = new String((byte[]) message.getPayload());
        JSONObject params = JSONUtil.parseObj(jsonString);
        Boolean isCollect = params.getBool("isCollect");
        Long noteId = params.getLong("noteId");
        Integer collectId = params.getInt("collectId");
        Integer userId = params.getInt("userId");
        try {
            noteService.changeCollectNoteWithTx(isCollect, noteId, collectId, userId);
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
        Integer isCollect = params.getInt("isCollect");
        Long noteId = params.getLong("noteId");
        Integer collectId = params.getInt("collectId");
        Long userId = params.getLong("userId");

        Note note = noteService.getOne(Wrappers.lambdaQuery(Note.class).eq(Note::getId, noteId).eq(Note::getUserId, userId).eq(Note::getCollectId, collectId).eq(Note::getIsCollect, isCollect));
        if (note != null) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
