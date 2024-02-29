package com.cloudnote.note.mq;

import com.cloudnote.note.service.INoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "note_insert_topic",consumerGroup = "note_insert_consumer_group")
public class NoteInsertListener implements RocketMQListener<MessageExt> {
    @Autowired
    private INoteService noteService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String str = new String(body);
        if (str.contains("\"")){
            str = str.replace("\"","");
        }
        Long noteId = Long.parseLong(str);

        noteService.addOrUpdateNoteById(noteId);
    }
}
