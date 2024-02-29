package com.cloudnote.record.service;


import com.cloudnote.record.api.dto.RecordDto;

import java.util.List;

/**
 * 历史记录相关操作
 */
public interface IRecordService {
    /**
     * 查询当前用户的所有历史记录
     *
     * @param userId
     * @return
     */
    public List<RecordDto> getRecords(Integer userId);

    /**
     * 添加记录
     * 最多保存10条记录
     *
     * @param recordVO
     * @param isRollBack
     
     */
    public boolean addRecord(RecordDto recordVO, Boolean isRollBack);


    /**
     * 删除记录
     * @param userId
     * @param recordId
     * @param type          0：小记，1：笔记
     * @param isRollBack
     
     */
    public boolean removeRecord(Integer userId, Long recordId, Integer type, Boolean isRollBack);

    /**
     * 获取普通删除的数据记录
     *
     * @param userId
     * @return
     
     */
    public List<RecordDto> getDeleteList(Integer userId);

    /**
     * 恢复一条数据
     *
     * @param recordId 记录id（笔记或小记）
     * @param type     类型（1：笔记，2：小记）
     * @param userId
     
     */
    public boolean restoreOne(Long recordId, Integer type, Integer userId);

    /**
     * 批量恢复数据
     *
     * @param bunches
     * @param userId
     
     */
    public void restoreBunches(List<String> bunches, Integer userId);

    /**
     * 彻底删除一条数据
     *
     * @param recordId
     * @param type
     * @param userId
     
     */
    public boolean completeDeleteOne(Long recordId, Integer type, Integer userId);

    /**
     * 批量彻底删除数据
     *
     * @param bunches
     * @param userId
     
     */
    public void completeDeleteBunches(List<String> bunches, Integer userId);
}
