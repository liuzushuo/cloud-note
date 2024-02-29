package com.cloudnote.collect.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.collect.mapper.ICollectMapper;
import com.cloudnote.collect.service.ICollectService;
import com.cloudnote.common.util.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cloudnote.common.constants.MqConstants.COLLECT_DELETE_TOPIC;
import static com.cloudnote.common.constants.MqConstants.COLLECT_INSERT_TOPIC;
import static com.cloudnote.common.constants.RedisConstants.CACHE_COLLECT_LIST_KEY;
import static com.cloudnote.common.constants.RedisConstants.CACHE_COLLECT_LIST_TTL;


@Slf4j
@Service
@Transactional
public class CollectServiceImpl extends ServiceImpl<ICollectMapper, Collect> implements ICollectService {
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 查询状态正常的收藏夹列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<Collect> getNormalCollectList(Integer userId) {
        // 1.查询
        List<Collect> collectList = cacheClient.queryListWithLogicalExpire(CACHE_COLLECT_LIST_KEY, userId, Collect.class, this::getCollectList, CACHE_COLLECT_LIST_TTL, TimeUnit.MINUTES);
        if (collectList == null || collectList.isEmpty()) {
            // 没有收藏夹
            return Collections.emptyList();
        }
        return collectList;
    }

    /**
     * 新建收藏夹
     *
     * @param collect
     */
    @Override
    public boolean createCollect(Collect collect) {
        boolean save = save(collect);
        // 3.发送消息
        JSONObject params = new JSONObject();
        params.put("userId", collect.getUserId());
        params.put("collectId", collect.getId());
        rocketMQTemplate.convertAndSend(COLLECT_INSERT_TOPIC, params.toString());
        return save;
    }

    /**
     * 根据用户id查询收藏夹列表
     *
     * @param userId
     * @return
     */
    private List<Collect> getCollectList(Integer userId) {
        // 构造查询条件
        LambdaQueryWrapper<Collect> lambdaQueryWrapper = Wrappers.lambdaQuery(Collect.class).eq(Collect::getUserId, userId);
        //  查询数据库
        return list(lambdaQueryWrapper);
    }

    /**
     * 创建用户默认收藏夹
     *
     * @param userId
     */
    @Override
    public void createDefaultCollect(Integer userId) {
        Collect collect = Collect.builder()
                .name("默认收藏夹")
                .count(0)
                .isDefault(true)
                .userId(userId)
                .build();
        save(collect);
        // 3.发送消息
        JSONObject params = new JSONObject();
        params.put("userId", collect.getUserId());
        params.put("collectId", collect.getId());
        rocketMQTemplate.convertAndSend(COLLECT_INSERT_TOPIC, params.toString());
    }

    /**
     * 根据id获取收藏夹信息
     *
     * @param collectId
     * @param userId
     * @return
     */
    @Override
    public Collect getCollectById(Integer collectId, Integer userId) {
        // 1.查询收藏夹列表
        List<Collect> collectList = getNormalCollectList(userId);
        // 2.根据id获取收藏夹信息
        List<Collect> res = collectList.stream().filter(collect -> collect.getId().equals(collectId)).collect(Collectors.toList());
        if (res == null || res.size() == 0) {
            // 没有找到收藏夹
            return null;
        }
        // 返回查询结果
        return res.get(0);
    }

    /**
     * 获取默认收藏夹id
     *
     * @param userId
     * @return
     */
    @Override
    public Integer getDefaultCollectId(Integer userId) {
        // 1.查询收藏夹列表
        List<Collect> collectList = getNormalCollectList(userId);
        // 2.根据id获取收藏夹信息
        List<Collect> res = collectList.stream().filter(collect -> collect.getIsDefault()).collect(Collectors.toList());
        if (res == null || res.size() == 0) {
            // 没有找到收藏夹
            return null;
        }
        // 返回查询结果
        return res.get(0).getId();
    }

    /**
     * 修改收藏夹
     *
     * @param collect
     */
    @Override
    public boolean updateCollect(Collect collect) {
        UpdateChainWrapper<Collect> wrapper = update().eq("id", collect.getId()).eq("user_id", collect.getUserId());
        if (collect.getName() != null) {
            wrapper.set("name", collect.getName());
        }
        if (collect.getCount() != null) {
            wrapper.setSql("count = count + " + collect.getCount());
        }
        boolean update = wrapper.update();
        // 发送消息
        JSONObject params = new JSONObject();
        params.put("userId", collect.getUserId());
        params.put("collectId", collect.getId());
        rocketMQTemplate.convertAndSend(COLLECT_INSERT_TOPIC, params.toString());
        return update;
    }


    /**
     * 删除收藏夹
     *
     * @param collectId
     * @param userId
     */
    @Override
    public boolean deleteCollect(Integer collectId, Integer userId) {
        // 1。查询收藏夹
        Collect collect = getCollectById(collectId, userId);
        if (collect == null) {
            // 收藏夹不存在
            return false;
        }
        // 2.判断当前收藏夹下是否有收藏的笔记
        if (collect.getCount() > 0) {
            // 收藏夹不为空
            return false;
        }
        // 3.判断是否为默认收藏夹
        if (collect.getIsDefault()) {
            // 默认收藏夹，不能删除
            return false;
        }
        // 4.删除收藏夹
        boolean remove = removeById(collectId);
        // 5.发送消息
        // 3.发送消息
        JSONObject params = new JSONObject();
        params.put("userId", collect.getUserId());
        params.put("collectId", collect.getId());
        rocketMQTemplate.convertAndSend(COLLECT_DELETE_TOPIC, params.toString());
        return remove;
    }

    /**
     * 将收藏夹添加到缓存
     *
     * @param collectId
     */
    @Override
    public void addToCache(Integer collectId,Integer userId) {
        // 1.查询收藏夹
        Collect collect = getById(collectId);
        // 2.获取收藏夹列表
        List<Collect> collectList = getNormalCollectList(userId);
        if (collectList == null || collectList.isEmpty()) {
            // 第一次添加，直接写入缓存
            cacheClient.setWithLogicalExpire(CACHE_COLLECT_LIST_KEY + userId, Collections.singletonList(collect), CACHE_COLLECT_LIST_TTL, TimeUnit.MINUTES);
        }
        // 3.将原收藏夹删除
        collectList = collectList.stream().filter(c -> !c.getId().equals(collectId)).collect(Collectors.toList());
        // 4.添加到列表中
        collectList.add(collect);
        // 5.更新缓存
        cacheClient.setWithLogicalExpire(CACHE_COLLECT_LIST_KEY + userId, collectList, CACHE_COLLECT_LIST_TTL, TimeUnit.MINUTES);
    }

    /**
     * 将收藏夹从缓存中删除
     *
     * @param collectId
     */
    @Override
    public void deleteFromCache(Integer collectId,Integer userId) {
        // 1.获取收藏夹列表
        List<Collect> collectList = getNormalCollectList(userId);
        // 2.将原收藏夹删除
        collectList = collectList.stream().filter(c -> !c.getId().equals(collectId)).collect(Collectors.toList());
        // 3.更新缓存
        cacheClient.setWithLogicalExpire(CACHE_COLLECT_LIST_KEY + userId, collectList, CACHE_COLLECT_LIST_TTL, TimeUnit.MINUTES);
    }
}