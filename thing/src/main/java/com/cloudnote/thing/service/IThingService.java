package com.cloudnote.thing.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.thing.api.domain.Thing;

import java.util.List;

public interface IThingService extends IService<Thing> {


    /**
     * 查询用户正常的小记
     *
     * @param search 查询关键词（标题或标签中含有关键词）
     * @param filter 过滤条件（null:默认，0：未完成，1：已完成）
     * @param userId 用户id
     * @return
     
     */
    public List<Thing> getUserNormalThing(String search, Integer filter, Integer userId);

    /**
     * 小记置顶状态修改
     *
     * @param isTop   是否置顶
     * @param thingId 小记id
     * @param userId  登录用户的id
     
     */
    public void topThing(boolean isTop, Integer thingId, Integer userId);

    /**
     * 根据id删除小记
     *
     * @param complete     是否为彻底删除
     * @param thingId
     * @param userId
     * @param isRecycleBin 是否是回收站中的操作
     
     */
    public boolean deleteThingById(Boolean complete, Integer thingId, Integer userId, Boolean isRecycleBin);

    /**
     * 新增小记
     *
     * @param thing
     
     */
    public void createThing(Thing thing);

    /**
     * 根据id获取小记
     *
     * @param thingId
     * @param userId
     * @return
     
     */
    public Thing getThingById(Integer thingId, Integer userId);

    /**
     * 修改小记
     *
     * @param thing
     
     */
    public boolean updateThing(Thing thing);


    /**
     * 恢复小记（将小记由删除状态恢复到正常状态）
     *
     * @param thingId
     * @param userId
     
     */
    public boolean restoreOneThing(Integer thingId, Integer userId);

    /**
     * 批量恢复小记
     *
     * @param thingIds
     * @param userId
     
     */
    public void restoreBunchesThing(List<Integer> thingIds, Integer userId);

    /**
     * 批量彻底删除小记
     * @param thingIds
     * @param userId
     
     */
    public void completeDeleteBunchesThing(List<Integer> thingIds,Integer userId);

    /**
     * 获取普通删除的小记列表
     * @param userId
     * @return
     
     */
    List<Thing> getDeleteThingList(Integer userId);

    /**
     * 添加或更新小记（ES和缓存）
     * @param thingId
     */
    void addOrUpdateById(Integer thingId);

    /**
     * 删除小记信息（ES和缓存）
     * @param thingId
     * @param userId
     */
    void deleteById(Integer thingId,Integer userId);

}