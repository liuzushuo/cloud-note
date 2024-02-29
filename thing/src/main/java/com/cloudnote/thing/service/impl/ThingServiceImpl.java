package com.cloudnote.thing.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.util.CacheClient;
import com.cloudnote.record.api.RemoteRecordService;
import com.cloudnote.record.api.dto.RecordDto;
import com.cloudnote.thing.api.domain.Thing;
import com.cloudnote.thing.mapper.IThingMapper;
import com.cloudnote.thing.service.IThingService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cloudnote.common.constants.MqConstants.THING_DELETE_TOPIC;
import static com.cloudnote.common.constants.MqConstants.THING_INSERT_TOPIC;
import static com.cloudnote.common.constants.RedisConstants.CACHE_THING_LIST_KEY;
import static com.cloudnote.common.constants.RedisConstants.CACHE_THING_LIST_TTL;


@Service
@Transactional
public class ThingServiceImpl extends ServiceImpl<IThingMapper, Thing> implements IThingService {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private RemoteRecordService recordService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 查询用户正常的小记
     *
     * @param search 查询关键词
     * @param filter 过滤条件（null:默认，0：未完成，1：已完成）
     * @param userId 用户id
     * @return
     */
    @Override
    public List<Thing> getUserNormalThing(String search, Integer filter, Integer userId) {
        // 1.查询
        List<Thing> thingList = cacheClient.queryListWithLogicalExpire(CACHE_THING_LIST_KEY, userId, Thing.class, this::getThingList, CACHE_THING_LIST_TTL, TimeUnit.MINUTES);
        if (thingList == null || thingList.isEmpty()) {
            // 没有小记
            return Collections.emptyList();
        }
        // 2.过滤
        if (filter != null) {
            thingList = thingList.stream().filter(thing -> thing.getFinished().equals(filter)).collect(Collectors.toList());
        }
        // 3.搜索
        if (StrUtil.isNotBlank(search)) {
            thingList = thingList.stream().filter(thing -> thing.getTitle().contains(search)).collect(Collectors.toList());
        }
        // 4.返回
        return thingList;
    }

    /**
     * 根据用户id获取小记列表
     *
     * @param userId
     * @return
     */
    private List<Thing> getThingList(Integer userId) {
        // 构造查询条件
        LambdaQueryWrapper<Thing> lambdaQueryWrapper = Wrappers.lambdaQuery(Thing.class)
                .eq(Thing::getUserId, userId).eq(Thing::getStatus, 1)
                .orderByDesc(Thing::getTop, Thing::getUpdateTime);
        //  查询数据库
        return list(lambdaQueryWrapper);
    }

    /**
     * 小记置顶状态修改
     *
     * @param isTop   是否置顶
     * @param thingId 小记id
     * @param userId  登录用户的id
     */
    @Override
    public void topThing(boolean isTop, Integer thingId, Integer userId) {
        update(Wrappers.lambdaUpdate(Thing.class).set(Thing::getTop, isTop).eq(Thing::getId, thingId).eq(Thing::getUserId, userId));
        // 发送修改小记的消息
        rocketMQTemplate.convertAndSend(THING_INSERT_TOPIC, thingId);
    }

    /**
     * 根据id删除小记
     *
     * @param complete     是否为彻底删除
     * @param thingId
     * @param userId
     * @param isRecycleBin 是否是回收站中的操作
     */
    @Override
    public boolean deleteThingById(Boolean complete, Integer thingId, Integer userId, Boolean isRecycleBin) {

        // 默认为正常删除，不是彻底删除
        // 删除前的状态值
        int beforeStatus = 1;
        // 删除后的状态值
        int afterStatus = 0;

        if (complete) {
            afterStatus = -1;
            if (isRecycleBin) {
                // 回收站中的小记状态都是已删除的
                beforeStatus = 0;
            }
        }
        // 1.查询小记
        Thing thing = getOne(Wrappers.lambdaQuery(Thing.class).eq(Thing::getId, thingId).eq(Thing::getUserId, userId).eq(Thing::getStatus, beforeStatus));
        if (thing == null) {
            return false;
        }
        // 2.更新小记状态
        boolean update = update(Wrappers.lambdaUpdate(Thing.class).set(Thing::getStatus, afterStatus).eq(Thing::getId, thingId).eq(Thing::getUserId, userId));

        // TODO:删除redis中的记录
        R recordResp = recordService.removeRecord(userId, Long.parseLong(thingId.toString()), 0, true);

        // 发消息
        JSONObject params = new JSONObject();
        params.put("userId", userId);
        params.put("thingId", thingId);
        rocketMQTemplate.convertAndSend(THING_DELETE_TOPIC, params.toString());
        return update;
    }

    /**
     * 新增小记
     *
     * @param thing
     */
    @Override
    public void createThing(Thing thing) {
        // 1.新增小记
        save(thing);
        // 2.查新新增的小记
        Thing createOne = getById(thing.getId());
        // TODO:添加操作记录到redis
        RecordDto recordVO = RecordDto.builder()
                .thingId(createOne.getId())
                .userId(createOne.getUserId())
                .title(createOne.getTitle())
                .updateTime(createOne.getUpdateTime())
                .type(2)
                .build();
        R recordResp = recordService.addRecord(recordVO, true);

        // 发送新增小记的消息
        rocketMQTemplate.convertAndSend(THING_INSERT_TOPIC, createOne.getId());
    }

    /**
     * 根据id获取小记
     *
     * @param thingId
     * @param userId
     * @return
     */
    @Override
    public Thing getThingById(Integer thingId, Integer userId) {
        // 1.查询小记列表
        List<Thing> thingList = getUserNormalThing(null, null, userId);
        if (thingList == null || thingList.isEmpty()) {
            // 没有小记
            return null;
        }
        // 2.根据id获取小记
        List<Thing> collect = thingList.stream().filter(thing -> thing.getId().equals(thingId)).collect(Collectors.toList());
        if (collect == null || collect.size() == 0) {
            return null;
        }
        Thing thing = collect.get(0);
        return thing;
    }

    /**
     * 修改小记
     *
     * @param thing
     */
    @Override
    public boolean updateThing(Thing thing) {
        // 1.更新小记
        boolean update = updateById(thing);
        // 2.查询更新后的小记
        Thing updateOne = getById(thing.getId());
        // TODO:3.添加操作记录到redis
        RecordDto recordVO = RecordDto.builder()
                .thingId(updateOne.getId())
                .userId(updateOne.getUserId())
                .title(updateOne.getTitle())
                .updateTime(updateOne.getUpdateTime())
                .type(2)
                .build();
        R recordResp = recordService.addRecord(recordVO, true);

        // 发送修改小记的消息
        rocketMQTemplate.convertAndSend(THING_INSERT_TOPIC, thing.getId());
        return update;
    }

    /**
     * 恢复小记（将小记由删除状态恢复到正常状态）
     *
     * @param thingId
     * @param userId
     */
    @Override
    public boolean restoreOneThing(Integer thingId, Integer userId) {
        // 修改小记状态
        boolean update = update(Wrappers.lambdaUpdate(Thing.class).set(Thing::getStatus, 1).eq(Thing::getId, thingId).eq(Thing::getUserId, userId).eq(Thing::getStatus, 0));
        // 发送修改小记的消息
        rocketMQTemplate.convertAndSend(THING_INSERT_TOPIC, thingId);
        return update;
    }

    /**
     * 批量恢复小记
     *
     * @param thingIds
     * @param userId
     */
    @Override
    public void restoreBunchesThing(List<Integer> thingIds, Integer userId) {
        // 参数校验
        if (thingIds == null || thingIds.size() == 0) return;
        // 批量恢复
        for (Integer thingId : thingIds) {
            restoreOneThing(thingId, userId);
        }
    }

    /**
     * 批量彻底删除小记
     *
     * @param thingIds
     * @param userId
     */
    @Override
    public void completeDeleteBunchesThing(List<Integer> thingIds, Integer userId) {
        if (thingIds == null || thingIds.size() == 0) return;
        // 批量删除小记
        for (Integer thingId : thingIds) {
            deleteThingById(true, thingId, userId, true);
        }
    }

    /**
     * 获取普通删除的小记列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<Thing> getDeleteThingList(Integer userId) {
        LambdaQueryWrapper<Thing> lambdaQueryWrapper = Wrappers.lambdaQuery(Thing.class).eq(Thing::getUserId, userId).eq(Thing::getStatus, 0);
        List<Thing> thingList = null;
        try {
            thingList = list(lambdaQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (thingList == null || thingList.size() == 0){
            return null;
        }
        return thingList;
    }

    /**
     * 添加或更新小记（ES和缓存）
     *
     * @param thingId
     */
    @Override
    public void addOrUpdateById(Integer thingId) {
        // 1.根据id查询小记信息
        Thing thing = getById(thingId);
        // 2.添加到缓存
        addToCache(thing);
        // 3.添加小记信息到ES
        addToES(thing);
    }

    /**
     * 添加小记信息到缓存
     *
     * @param thing
     */
    public void addToCache(Thing thing) {
        // 1.获取小记列表
        Integer userId = thing.getUserId();
        List<Thing> thingList = getUserNormalThing(null, null, userId);
        if (thingList == null || thingList.size() == 0) {
            // 第一次添加，直接写入缓存
            cacheClient.setWithLogicalExpire(CACHE_THING_LIST_KEY + userId, Collections.singletonList(thing), CACHE_THING_LIST_TTL, TimeUnit.MINUTES);
        }
        // 2.将原有的小记删除
        thingList = thingList.stream().filter(t -> !t.getId().equals(thing.getId())).collect(Collectors.toList());
        // 3.添加新的小记
        thingList.add(thing);
        // 4.更新缓存
        cacheClient.setWithLogicalExpire(CACHE_THING_LIST_KEY + userId, thingList, CACHE_THING_LIST_TTL, TimeUnit.MINUTES);
    }

    /**
     * 添加小记信息到ES
     *
     * @param thing
     */
    private void addToES(Thing thing) {
        // 1.创建请求对象
        IndexRequest request = new IndexRequest("thing").id(thing.getId().toString());
        // 2.准备参数
        request.source(JSONUtil.toJsonStr(thing), XContentType.JSON);
        // 3.发送请求
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除小记信息（ES和缓存）
     *
     * @param thingId
     * @param userId
     */
    @Override
    public void deleteById(Integer thingId, Integer userId) {
        // 1.从缓存删除小记信息
        deleteThingFromCache(thingId, userId);
        // 2.从ES删除小记信息
        deleteThingFromES(thingId);
    }


    /**
     * 从ES删除小记信息
     *
     * @param thingId
     */
    private void deleteThingFromES(Integer thingId) {
        // 1.创建请求对象
        DeleteRequest request = new DeleteRequest("thing", thingId.toString());
        // 2.发送请求
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存删除小记信息
     *
     * @param thingId
     */
    private void deleteThingFromCache(Integer thingId, Integer userId) {
        // 1.获取小记列表
        List<Thing> thingList = getUserNormalThing(null, null, userId);
        // 2.删除对应的小记
        thingList = thingList.stream().filter(thing -> !thing.getId().equals(thingId)).collect(Collectors.toList());
        // 3.更新缓存
        cacheClient.setWithLogicalExpire(CACHE_THING_LIST_KEY + userId, thingList, CACHE_THING_LIST_TTL, TimeUnit.MINUTES);
    }
}