package com.cloudnote.record.service.impl;

import cn.hutool.json.JSONUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.Constants;
import com.cloudnote.note.api.RemoteNoteService;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.record.api.dto.RecordDto;
import com.cloudnote.record.service.IRecordService;
import com.cloudnote.thing.api.RemoteThingService;
import com.cloudnote.thing.api.domain.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cloudnote.common.constants.RedisConstants.CACHE_RECORD_LIST_KEY;


@Service
public class RecordServiceImpl implements IRecordService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RemoteNoteService noteService;
    @Autowired
    private RemoteThingService thingService;

    /**
     * 查询当前用户的所有历史记录
     *
     * @param userId
     * @return
     */
    @Override
    public List<RecordDto> getRecords(Integer userId) {
        // 构造redis查询的key
        String key = CACHE_RECORD_LIST_KEY + userId;
        // 获取所有历史记录
        Set<String> set = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        return set.stream().map(json -> JSONUtil.toBean(json, RecordDto.class)).collect(Collectors.toList());
    }

    /**
     * 添加记录
     * 最多保存10条记录
     *
     * @param recordVO
     * @param isRollBack
     */
    @Override
    public boolean addRecord(RecordDto recordVO, Boolean isRollBack) {
        // 构造key
        String key = CACHE_RECORD_LIST_KEY + recordVO.getUserId();

        Long noteId = recordVO.getNoteId();
        Integer thingId = recordVO.getThingId();

        // 查询所有的记录
        Set<String> set = redisTemplate.opsForZSet().range(key, 0, -1);
        // 遍历集合，判断当前记录是否已存储
        for (String json : set) {
            RecordDto record = JSONUtil.toBean(json, RecordDto.class);
            // 删除重复的笔记
            if (noteId != null && record.getNoteId() != null && noteId.equals(record.getNoteId())) {
                redisTemplate.opsForZSet().remove(key, json);
            }
            // 删除重复的小记
            if (thingId != null && record.getThingId() != null && thingId.equals(record.getThingId())) {
                redisTemplate.opsForZSet().remove(key, json);
            }
        }

        // 存入redis
        boolean add = false;
        try {
            add = redisTemplate.opsForZSet().add(key, JSONUtil.toJsonStr(recordVO), recordVO.getUpdateTime().getTime() / 1000);
            // 判断当前zset中的元素是否超出10个，如果超出则删除最后一个
            Long size = redisTemplate.opsForZSet().size(key);
            if (size > 10) {
                Set oldRecords = redisTemplate.opsForZSet().reverseRange(key, 10, -1);
                redisTemplate.opsForZSet().remove(key, oldRecords.toArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (!add) {
            return false;
        }
        return true;
    }

    /**
     * 删除记录
     *
     * @param userId
     * @param recordId
     * @param type       0：小记，1：笔记
     * @param isRollBack
     */
    @Override
    public boolean removeRecord(Integer userId, Long recordId, Integer type, Boolean isRollBack) {
        Long noteId = null;
        Integer thingId = null;
        if (type.equals(1)) {
            noteId = recordId;
        } else {
            thingId = recordId.intValue();
        }
        // 构造key
        String key = CACHE_RECORD_LIST_KEY + userId;
        // 构造条件
        try {
            // 查询所有的记录
            Set<String> set = redisTemplate.opsForZSet().range(key, 0, -1);
            // 遍历集合，判断当前记录是否已存储
            for (String json : set) {
                RecordDto record = JSONUtil.toBean(json, RecordDto.class);
                // 移除小记记录
                if (type == 2 && record.getThingId() != null && record.getThingId().equals(thingId)) {
                    // 已存储，则删除该记录
                    redisTemplate.opsForZSet().remove(key, JSONUtil.toJsonStr(record));
                    break;
                }
                // 移除笔记记录
                if (type == 1 && record.getNoteId() != null && record.getNoteId().equals(noteId)) {
                    // 已存储，则删除该记录
                    redisTemplate.opsForZSet().remove(key, JSONUtil.toJsonStr(record));
                    break;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取普通删除的数据记录
     *
     * @param userId
     * @return
     */
    @Override
    public List<RecordDto> getDeleteList(Integer userId) {
        // 1.查询笔记、小记列表
        R noteResp = noteService.getDeleteNoteList(userId);
        List<Note> noteList = Collections.emptyList();
        if (noteResp.getCode() == Constants.SUCCESS) {
            noteList = JSONUtil.toList((String) noteResp.getData(), Note.class);
        }

        R thingResp = thingService.getDeleteThingList(userId);
        List<Thing> thingList = Collections.emptyList();
        if (thingResp.getCode() == Constants.SUCCESS) {
            thingList = JSONUtil.toList((String) thingResp.getData(), Thing.class);
        }


        // 2.构造返回值
        List<RecordDto> recordVOList = new ArrayList<>();
        noteList.forEach(note -> {
            RecordDto recordVO = RecordDto.builder()
                    .noteId(note.getId())
                    .title(note.getTitle())
                    .type(1)
                    .updateTime(note.getUpdateTime())
                    .build();
            recordVOList.add(recordVO);
        });
        thingList.forEach(thing -> {
            RecordDto recordVO = RecordDto.builder()
                    .thingId(thing.getId())
                    .title(thing.getTitle())
                    .type(2)
                    .updateTime(thing.getUpdateTime())
                    .build();
            recordVOList.add(recordVO);
        });
        return recordVOList;
    }

    /**
     * 恢复一条数据
     *
     * @param recordId 记录id（笔记或小记）
     * @param type     类型（1：笔记，2：小记）
     * @param userId
     */
    @Override
    public boolean restoreOne(Long recordId, Integer type, Integer userId) {
        Long noteId = null;
        Integer thingId = null;
        if (type.equals(1)) {
            noteId = recordId;
        } else {
            thingId = recordId.intValue();
        }
        if (type.equals(1)) {
            R noteResp = noteService.restoreOneNote(noteId, userId);
            if (noteResp.getCode() != Constants.SUCCESS) {
                return false;
            }
        } else if (type.equals(2)) {
            R thingResp = thingService.restoreOneThing(thingId, userId);
            if (thingResp.getCode() != Constants.SUCCESS) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * 批量恢复数据
     *
     * @param bunches
     * @param userId
     */
    @Override
    public void restoreBunches(List<String> bunches, Integer userId) {
        if (bunches == null || bunches.size() == 0) return;
        // 1.解析数据
        // 元素格式：id:type
        // 需要将type相同的id放入同一个List集合中
        List<Long> noteIdList = new ArrayList<>();
        List<Integer> thingIdList = new ArrayList<>();
        bunches.forEach(str -> {
            Long id = Long.parseLong(str.split(":")[0]);
            Integer type = Integer.parseInt(str.split(":")[1]);
            if (type.equals(1)) {
                noteIdList.add(id);
            } else {
                thingIdList.add(id.intValue());
            }
        });
        // 2.调用service批量恢复
        noteService.restoreBunchesNote(noteIdList, userId);
        thingService.restoreBunchesThing(thingIdList, userId);
    }

    /**
     * 彻底删除一条数据
     *
     * @param recordId
     * @param type
     * @param userId
     */
    @Override
    public boolean completeDeleteOne(Long recordId, Integer type, Integer userId) {
        Long noteId = null;
        Integer thingId = null;
        if (type.equals(1)) {
            noteId = recordId;
        } else {
            thingId = recordId.intValue();
        }
        if (type.equals(1)) {
            noteService.deleteNoteById(noteId, userId);
        } else if (type.equals(2)) {
            thingService.deleteThingById(thingId, userId);
        } else {
            return false;
        }
        return true;
    }

    /**
     * 批量彻底删除数据
     *
     * @param bunches
     * @param userId
     */
    @Override
    public void completeDeleteBunches(List<String> bunches, Integer userId) {
        if (bunches == null || bunches.size() == 0) return;
        // 1.解析数据
        // 元素格式：id:type
        // 需要将type相同的id放入同一个List集合中
        List<Long> noteIdList = new ArrayList<>();
        List<Integer> thingIdList = new ArrayList<>();
        bunches.forEach(str -> {
            Long id = Long.parseLong(str.split(":")[0]);
            Integer type = Integer.parseInt(str.split(":")[1]);
            if (type.equals(1)) {
                noteIdList.add(id);
            } else {
                thingIdList.add(id.intValue());
            }
        });
        // 2.调用service批量彻底删除数据
        noteService.completeDeleteBunchesNote(noteIdList, userId);
        thingService.completeDeleteBunchesThing(thingIdList, userId);
    }
}
