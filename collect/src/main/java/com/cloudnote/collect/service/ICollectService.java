package com.cloudnote.collect.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.collect.api.domain.Collect;

import java.util.List;

public interface ICollectService extends IService<Collect> {

    /**
     * 查询状态正常的收藏夹列表
     *
     * @param userId
     * @return
     
     */
    public List<Collect> getNormalCollectList(Integer userId);

    /**
     * 新建收藏夹
     *
     * @param collect
     */
    public boolean createCollect(Collect collect);

    /**
     * 创建用户默认收藏夹
     * @param userId
     
     */
    public void createDefaultCollect(Integer userId);

    /**
     * 根据id获取收藏夹信息
     *
     * @param collectId
     * @param userId
     * @return
     
     */
    public Collect getCollectById(Integer collectId, Integer userId);

    /**
     * 获取默认收藏夹id
     *
     * @param userId
     * @return
     
     */
    public Integer getDefaultCollectId(Integer userId);

    /**
     * 修改收藏夹
     *
     * @param collect
     
     */
    public boolean updateCollect(Collect collect);

    /**
     * 删除收藏夹
     *
     * @param collectId
     * @param userId
     
     */
    public boolean deleteCollect(Integer collectId, Integer userId);

    /**
     * 将收藏夹添加到缓存
     * @param collectId
     * @param userId
     */
    public void addToCache(Integer collectId,Integer userId);

    /**
     *  将收藏夹从缓存中删除
     * @param collectId
     * @param userId
     */
    public void deleteFromCache(Integer collectId,Integer userId);
}