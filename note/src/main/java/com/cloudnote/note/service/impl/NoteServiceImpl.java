package com.cloudnote.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.collect.api.RemoteCollectService;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.Constants;
import com.cloudnote.common.constants.HttpStatus;
import com.cloudnote.common.constants.MqConstants;
import com.cloudnote.common.util.CacheClient;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.note.api.domain.NoteBody;
import com.cloudnote.note.api.dto.NoteDTO;
import com.cloudnote.note.mapper.INoteBodyMapper;
import com.cloudnote.note.mapper.INoteMapper;
import com.cloudnote.note.service.INoteService;
import com.cloudnote.record.api.RemoteRecordService;
import com.cloudnote.record.api.dto.RecordDto;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cloudnote.common.constants.MqConstants.NOTE_COLLECT_TX_GROUP;
import static com.cloudnote.common.constants.MqConstants.NOTE_COLLECT_TX_TOPIC;
import static com.cloudnote.common.constants.RedisConstants.CACHE_NOTE_LIST_KEY;
import static com.cloudnote.common.constants.RedisConstants.CACHE_NOTE_LIST_TTL;


@Service
public class NoteServiceImpl extends ServiceImpl<INoteMapper, Note> implements INoteService {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private INoteBodyMapper noteBodyMapper;
    @Autowired
    private RemoteRecordService recordService;
    @Autowired
    private RemoteCollectService collectService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 创建笔记并初始化
     *
     * @param level
     * @param userId
     * @return
     */
    @Override
    public Long createNoteInit(Integer level, Integer userId) {
        // 1.非会员用户需要先判断笔记总数
        if (level == 0) {
            Long noteCount = getUserNoteCount(userId);
            if (noteCount >= 100) {
                // 如果笔记总数大于等于100，则不允许再新增笔记
                return null;
            }
        }

        // 2.构建笔记对象
        // TODO:获取默认收藏夹
        R collectResp = collectService.getDefaultCollectId(userId);
        if (collectResp.getCode() != Constants.SUCCESS) {
            return null;
        }
        Integer defaultCollectId = (Integer) collectResp.getData();

        Long noteId = IdUtil.getSnowflakeNextId();
        Note note = Note.builder()
                .id(noteId)
                .userId(userId)
                .collectId(defaultCollectId)
                .build();
        // 3.添加到数据库
        boolean save = false;
        try {
            save = save(note);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (!save) {
            return null;
        }
        // 4.获取创建的笔记
        Note createOne = getById(noteId);

        // TODO:添加操作记录到
        RecordDto recordVO = RecordDto.builder()
                .noteId(createOne.getId())
                .userId(userId)
                .title("暂未设置标题")
                .updateTime(createOne.getUpdateTime())
                .type(1)
                .build();
        recordService.addRecord(recordVO, true);

        // 发送创建笔记的消息
        rocketMQTemplate.convertAndSend(MqConstants.NOTE_INSERT_TOPIC, createOne.getId());

        // 返回笔记id
        return noteId;
    }

    /**
     * 获取用户状态正常的笔记
     *
     * @param userId
     * @return
     */
    @Override
    public List<Note> getUserNormalNotes(Integer userId) {
        // 1.查询
        List<Note> noteList = cacheClient.queryListWithLogicalExpire(CACHE_NOTE_LIST_KEY, userId, Note.class, this::getNoteList, CACHE_NOTE_LIST_TTL, TimeUnit.MINUTES);
        if (noteList == null || noteList.isEmpty()) {
            // 没有笔记
            return Collections.emptyList();
        }
        // 2.返回查询结果
        return noteList;
    }

    /**
     * 根据用户id获取笔记列表
     *
     * @param userId
     * @return
     */
    private List<Note> getNoteList(Integer userId) {
        // 构造查询条件
        LambdaQueryWrapper<Note> lambdaQueryWrapper = Wrappers.lambdaQuery(Note.class)
                .select(Note::getId, Note::getTitle, Note::getTop, Note::getUpdateTime, Note::getType)
                .eq(Note::getUserId, userId).eq(Note::getStatus, 1)
                .orderByDesc(Note::getTop, Note::getUpdateTime);
        //  查询数据库
        return list(lambdaQueryWrapper);
    }

    /**
     * 根据id获取笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    @Override
    public NoteDTO getNoteById(Long noteId, Integer userId) {
        // 1.查询笔记列表
        List<Note> noteList = getUserNormalNotes(userId);
        if (noteList == null || noteList.isEmpty()) {
            // 没有笔记
            return null;
        }
        // 3.拿到笔记
        List<Note> collect = noteList.stream().filter(note -> note.getId().equals(noteId)).collect(Collectors.toList());
        if (collect == null || collect.size() == 0) {
            return null;
        }
        Note note = collect.get(0);
        // 4.查询笔记内容
        NoteBody noteBody = noteBodyMapper.selectOne(Wrappers.lambdaUpdate(NoteBody.class).eq(NoteBody::getNoteId, noteId));
        // 5.封装结果返回
        NoteDTO noteDTO = BeanUtil.copyProperties(note, NoteDTO.class);
        noteDTO.setBody(noteBody != null ? noteBody.getBody() : null);
        return noteDTO;
    }

    /**
     * 置顶笔记
     *
     * @param isTop  是否置顶
     * @param noteId 笔记id
     * @param userId 用户id
     */
    @Override
    public void topNote(Boolean isTop, Long noteId, Integer userId) {
        // 更新笔记置顶状态
        update(Wrappers.lambdaUpdate(Note.class).set(Note::getTop, isTop).eq(Note::getId, noteId).eq(Note::getUserId, userId));
        // 发送修改笔记的消息
        rocketMQTemplate.convertAndSend(MqConstants.NOTE_INSERT_TOPIC, noteId);
    }

    /**
     * 删除笔记
     *
     * @param complete     是否为彻底删除
     * @param noteId
     * @param userId
     * @param isRecycleBin 是否是回收站中的操作
     */
    @Override
    public boolean deleteNote(Boolean complete, Long noteId, Integer userId, Boolean isRecycleBin) {
        // 删除前的状态值
        int beforeStatus = 1;
        if (isRecycleBin) {
            // 回收站中的笔记状态都是已删除的
            beforeStatus = 0;
        }
        // 构造查询条件
        LambdaQueryWrapper<Note> lambdaQueryWrapper = Wrappers.lambdaQuery(Note.class).eq(Note::getId, noteId).eq(Note::getUserId, userId).eq(Note::getStatus, beforeStatus);
        Note deleteOne = getOne(lambdaQueryWrapper);
        if (deleteOne == null) {
            return false;
        }
        // TODO 开始事务，删除笔记并更新收藏夹的count
        JSONObject params = new JSONObject();
        params.put("complete", complete);
        params.put("noteId", noteId);
        params.put("userId", userId);
        params.put("isRecycleBin", isRecycleBin);
        params.put("collectId", deleteOne.getCollectId());
        params.put("isCollect", deleteOne.getIsCollect());
        params.put("count", -1);
        Message<String> message = MessageBuilder.withPayload(params.toString()).build();
        rocketMQTemplate.sendMessageInTransaction(MqConstants.NOTE_DELETE_TX_GROUP, MqConstants.NOTE_RESTOE_OR_DELETE_TX_TOPIC, message, null);
        return true;
    }

    /**
     * 删除笔记
     *
     * @param complete
     * @param noteId
     * @param userId
     * @param isRecycleBin
     */
    public boolean deleteNoteWithTX(Boolean complete, Long noteId, Integer userId, Boolean isRecycleBin) {
        // 删除前的状态值
        int beforeStatus = 1;
        // 删除后的状态值
        int afterStatus = 0;

        if (complete) {
            afterStatus = -1;
            if (isRecycleBin) {
                // 回收站中的笔记状态都是已删除的
                beforeStatus = 0;
            }
        }

        // 更新笔记状态
        boolean update = update(Wrappers.lambdaUpdate(Note.class).set(Note::getStatus, afterStatus).eq(Note::getId, noteId).eq(Note::getUserId, userId).eq(Note::getStatus, beforeStatus));
        if (!update) {
            return false;
        }

        // TODO:删除redis中的记录
        R recordResp = recordService.removeRecord(userId, noteId, 1, true);
        if (recordResp.getCode() != HttpStatus.SUCCESS) {
            return false;
        }

        // 更新缓存和ES
        deleteNoteById(noteId, userId);

        return true;
    }

    /**
     * 保存正在编辑的笔记
     *
     * @param noteId
     * @param userId
     * @param title  笔记标题
     * @param body   笔记内容
     * @return
     */
    @Override
    public Date saveEditingNote(Long noteId, Integer userId, String title, String body) {
        // 1.更新note
        boolean update = update(Wrappers.lambdaUpdate(Note.class).set(Note::getTitle, title).eq(Note::getId, noteId).eq(Note::getUserId, userId));

        // 2.更新note_body
        NoteBody noteBody = noteBodyMapper.selectOne(Wrappers.lambdaUpdate(NoteBody.class).eq(NoteBody::getNoteId, noteId));
        if (noteBody == null) {
            // 首次创建
            noteBody = NoteBody.builder().noteId(noteId).body(body).build();
            noteBodyMapper.insert(noteBody);
        } else {
            noteBodyMapper.update(NoteBody.builder().noteId(noteId).body(body).build(), Wrappers.lambdaUpdate(NoteBody.class).eq(NoteBody::getNoteId, noteId));
        }
        // 3.获取更新后的笔记
        Note note = getById(noteId);
        // 4.TODO:添加操作记录到redis
        RecordDto recordVO = RecordDto.builder()
                .noteId(noteId)
                .userId(userId)
                .title(title)
                .updateTime(note.getUpdateTime())
                .type(1)
                .build();
        R recordResp = recordService.addRecord(recordVO, true);

        // 发送修改笔记的消息
        rocketMQTemplate.convertAndSend(MqConstants.NOTE_INSERT_TOPIC, noteId);

        // 6.返回更新的时间
        return note.getUpdateTime();
    }

    /**
     * 恢复笔记（将笔记由删除状态恢复到正常状态）
     *
     * @param noteId
     * @param userId
     */
    @Override
    public boolean restoreOneNote(Long noteId, Integer userId) {
        // 1.查询笔记
        Note restoreOne = getOne(Wrappers.lambdaQuery(Note.class).eq(Note::getId, noteId).eq(Note::getUserId, userId).eq(Note::getStatus, 0));
        if (restoreOne == null) {
            return false;
        }
        // TODO 开始事务，恢复笔记并更新收藏夹的count
        JSONObject params = new JSONObject();
        params.put("noteId", noteId);
        params.put("userId", userId);
        params.put("collectId", restoreOne.getCollectId());
        params.put("isCollect", restoreOne.getIsCollect());
        params.put("count", 1);
        Message<String> message = MessageBuilder.withPayload(params.toString()).build();
        rocketMQTemplate.sendMessageInTransaction(MqConstants.NOTE_RESTORE_TX_GROUP, MqConstants.NOTE_RESTOE_OR_DELETE_TX_TOPIC, message, null);
        return true;
    }

    /**
     * 恢复一个笔记
     *
     * @param noteId
     * @param userId
     */
    public boolean restoreNoteWithTx(Long noteId, Integer userId) {
        // 1.更新笔记状态
        boolean update = update(Wrappers.lambdaUpdate(Note.class).set(Note::getStatus, 1).eq(Note::getId, noteId).eq(Note::getUserId, userId));
        if (!update) {
            return false;
        }
        // 2.查询笔记
        Note restoreOne = getById(noteId);
        // 更新缓存和ES
        addOrUpdateNoteById(noteId);
        return true;
    }

    /**
     * 批量恢复笔记
     *
     * @param noteIds
     * @param userId
     */
    @Override
    public void restoreBunchesNote(List<Long> noteIds, Integer userId) {
        // 参数校验
        if (noteIds == null || noteIds.size() == 0) return;
        // 批量恢复
        for (Long noteId : noteIds) {
            restoreOneNote(noteId, userId);
        }
    }

    /**
     * 批量彻底删除笔记
     *
     * @param noteIds
     * @param userId
     */
    @Override
    public void completeDeleteBunchesNote(List<Long> noteIds, Integer userId) {
        if (noteIds == null || noteIds.size() == 0) return;
        // 批量删除
        for (Long noteId : noteIds) {
            deleteNote(true, noteId, userId, true);
        }
    }

    /**
     * 查询收藏夹中收藏的笔记列表
     *
     * @param collectId 收藏夹id
     * @param userId
     * @return
     */
    @Override
    public List<Note> getCollectNoteList(Integer collectId, Integer userId) {
        // 1.获取用户笔记列表
        List<Note> noteList = getUserNormalNotes(userId);
        // 2.过滤收藏夹中的笔记
        noteList = noteList.stream().filter(note -> note.getCollectId() != null && note.getCollectId() == collectId && note.getIsCollect()).collect(Collectors.toList());
        // 3.返回查询结果
        return noteList;
    }

    /**
     * 修改笔记收藏状态
     *
     * @param isCollect 是否收藏
     * @param noteId
     * @param collectId
     * @param userId
     */
    @Override
    public boolean changeCollectNote(Boolean isCollect, Long noteId, Integer collectId, Integer userId) {
        // 1.查询笔记
        NoteDTO updateOne = getNoteById(noteId, userId);
        if (updateOne == null) {
            return false;
        }
        if (!isCollect && !updateOne.getIsCollect()) {
            // 未收藏的笔记，不能取消收藏
            return false;
        }

        // TODO 开始事务，修改笔记收藏状态并更新收藏夹的count
        JSONObject params = new JSONObject();
        params.put("isCollect", isCollect);
        params.put("noteId", noteId);
        params.put("collectId", collectId);
        params.put("userId", userId);
        params.put("count", isCollect ? 1 : -1);
        params.put("oldCollectStatus", updateOne.getIsCollect());
        params.put("oldCollectId", updateOne.getCollectId());
        Message<String> message = MessageBuilder.withPayload(params.toString()).build();
        rocketMQTemplate.sendMessageInTransaction(NOTE_COLLECT_TX_GROUP, NOTE_COLLECT_TX_TOPIC, message, null);
        return true;
    }

    /**
     * 修改笔记收藏状态
     *
     * @param isCollect
     * @param noteId
     * @param collectId
     * @param userId
     */
    public boolean changeCollectNoteWithTx(Boolean isCollect, Long noteId, Integer collectId, Integer userId) {
        // 1.修改笔记收藏状态
        boolean update = update(Wrappers.lambdaUpdate(Note.class).set(Note::getIsCollect, isCollect).set(Note::getCollectId, collectId).eq(Note::getId, noteId).eq(Note::getUserId, userId));
        if (!update) {
            return false;
        }
        // 更新缓存和ES
        addOrUpdateNoteById(noteId);
        return true;
    }

    /**
     * 获取普通删除的笔记列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<Note> getDeleteNoteList(Integer userId) {
        // 构造查询条件
        LambdaQueryWrapper<Note> lambdaQueryWrapper = Wrappers.lambdaQuery(Note.class).eq(Note::getUserId, userId).eq(Note::getStatus, 0);
        // 查询数据库
        List<Note> noteList = null;
        try {
            noteList = list(lambdaQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return noteList;
    }

    /**
     * 获取用户状态正常的笔记数
     *
     * @param userId
     * @return
     */
    @Override
    public Long getUserNoteCount(Integer userId) {
        // 1.查询
        List<Note> noteList = cacheClient.queryListWithLogicalExpire(CACHE_NOTE_LIST_KEY, userId, Note.class, this::getNoteList, CACHE_NOTE_LIST_TTL, TimeUnit.MINUTES);
        if (noteList == null) {
            return 0L;
        }
        // 2.返回结果
        return Long.valueOf(noteList.size());
    }

    /**
     * 删除笔记信息（ES和缓存）
     *
     * @param noteId
     * @param userId
     */
    @Override
    public void deleteNoteById(Long noteId, Integer userId) {
        // 1.从缓存删除笔记信息
        deleteFromCache(noteId, userId);
        // 2.从ES删除笔记信息
        deleteNoteFromES(noteId);
    }


    /**
     * 从缓存删除笔记信息
     *
     * @param noteId
     * @param userId
     */
    private void deleteFromCache(Long noteId, Integer userId) {
        String key = CACHE_NOTE_LIST_KEY + userId;
        List<Note> noteList = getUserNormalNotes(userId);
        // 删除原笔记
        noteList = noteList.stream().filter(n -> !n.getId().equals(noteId)).collect(Collectors.toList());
        // 存入缓存
        cacheClient.setWithLogicalExpire(key, noteList, CACHE_NOTE_LIST_TTL, TimeUnit.MINUTES);
    }

    /**
     * 从ES删除笔记信息
     *
     * @param noteId
     */
    private void deleteNoteFromES(Long noteId) {
        // 1.准备请求对象
        DeleteRequest request = new DeleteRequest("note", noteId.toString());
        // 2.发送请求
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加或更新笔记（ES和缓存）
     *
     * @param noteId
     */
    @Override
    public void addOrUpdateNoteById(Long noteId) {
        // 1.根据id查询笔记信息
        Note note = getById(noteId);
        NoteBody noteBody = noteBodyMapper.selectOne(Wrappers.lambdaUpdate(NoteBody.class).eq(NoteBody::getNoteId, noteId));
        NoteDTO noteDTO = BeanUtil.copyProperties(note, NoteDTO.class);
        noteDTO.setBody(noteBody != null ? noteBody.getBody() : null);
        // 2.添加到缓存
        addToCache(note);
        // 3.添加到ES
        addToES(noteDTO);
    }

    /**
     * 添加笔记到缓存
     *
     * @param note
     */
    private void addToCache(Note note) {
        Integer userId = note.getUserId();
        String key = CACHE_NOTE_LIST_KEY + userId;
        List<Note> noteList = getUserNormalNotes(userId);
        if (noteList == null || noteList.isEmpty()) {
            // 第一次添加，直接写入缓存
            cacheClient.setWithLogicalExpire(key, Collections.singletonList(note), CACHE_NOTE_LIST_TTL, TimeUnit.MINUTES);
            return;
        }
        // 删除原笔记
        noteList = noteList.stream().filter(n -> !n.getId().equals(note.getId())).collect(Collectors.toList());

        // 分类，分为置顶笔记和普通笔记
        LinkedList<Note> topNoteList = new LinkedList<>();
        LinkedList<Note> normalNoteList = new LinkedList<>();
        for (Note n : noteList) {
            if (n.getTop()){
                topNoteList.push(n);
            }else {
                normalNoteList.push(n);
            }
        }

        // 根据待添加笔记的top属性添加到对应的链表
        if (note.getTop()){
            topNoteList.push(note);
        }else {
            normalNoteList.push(note);
        }
        // 合并链表
        topNoteList.addAll(normalNoteList);

        // 存入缓存
        cacheClient.setWithLogicalExpire(key, topNoteList, CACHE_NOTE_LIST_TTL, TimeUnit.MINUTES);
    }

    /**
     * 添加笔记到ES
     *
     * @param note
     */
    private void addToES(NoteDTO note) {
        // 1.准备请求对象
        IndexRequest request = new IndexRequest("note").id(note.getId().toString());
        // 2.准备DSL
        request.source(JSONUtil.toJsonStr(note), XContentType.JSON);
        // 3.发送请求
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}