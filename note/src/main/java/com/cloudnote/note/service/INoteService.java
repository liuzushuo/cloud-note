package com.cloudnote.note.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.note.api.dto.NoteDTO;

import java.util.Date;
import java.util.List;

public interface INoteService extends IService<Note> {


    /**
     * 添加或更新笔记（ES和缓存）
     *
     * @param noteId
     */
    void addOrUpdateNoteById(Long noteId);

    /**
     * 创建笔记并初始化
     *
     * @param level
     * @param userId
     * @return
     */
    Long createNoteInit(Integer level, Integer userId);

    /**
     * 获取用户状态正常的笔记
     *
     * @param userId
     * @return
     */
    List<Note> getUserNormalNotes(Integer userId);

    /**
     * 根据id获取笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    public NoteDTO getNoteById(Long noteId, Integer userId);

    /**
     * 置顶笔记
     *
     * @param isTop  是否置顶
     * @param noteId 笔记id
     * @param userId 用户id
     */
    void topNote(Boolean isTop, Long noteId, Integer userId);

    /**
     * 删除笔记
     *
     * @param complete     是否为彻底删除
     * @param noteId
     * @param userId
     * @param isRecycleBin 是否是回收站中的操作
     */
    public boolean deleteNote(Boolean complete, Long noteId, Integer userId, Boolean isRecycleBin);

    /**
     * 删除笔记
     *
     * @param complete
     * @param noteId
     * @param userId
     * @param isRecycleBin
     */
    public boolean deleteNoteWithTX(Boolean complete, Long noteId, Integer userId, Boolean isRecycleBin);

    /**
     * 保存正在编辑的笔记
     *
     * @param noteId
     * @param userId
     * @param title   笔记标题
     * @param body    笔记内容
     * @return
     
     */
    public Date saveEditingNote(Long noteId, Integer userId, String title, String body);

    /**
     * 恢复笔记（将笔记由删除状态恢复到正常状态）
     *
     * @param noteId
     * @param userId
     
     */
    public boolean restoreOneNote(Long noteId, Integer userId);

    /**
     * 恢复一个笔记
     *
     * @param noteId
     * @param userId
     
     */
    public boolean restoreNoteWithTx(Long noteId, Integer userId);

    /**
     * 批量恢复笔记
     *
     * @param noteIds
     * @param userId
     
     */
    public void restoreBunchesNote(List<Long> noteIds, Integer userId);

    /**
     * 批量彻底删除笔记
     *
     * @param noteIds
     * @param userId
     
     */
    public void completeDeleteBunchesNote(List<Long> noteIds, Integer userId);

    /**
     * 查询收藏夹中收藏的笔记列表
     *
     * @param collectId 收藏夹id
     * @param userId
     * @return
     
     */
    public List<Note> getCollectNoteList(Integer collectId, Integer userId);

    /**
     * 修改笔记收藏状态
     *
     * @param isCollect 是否收藏
     * @param noteId
     * @param collectId
     * @param userId

     */
    public boolean changeCollectNote(Boolean isCollect, Long noteId, Integer collectId, Integer userId);

    /**
     * 修改笔记收藏状态
     *
     * @param isCollect
     * @param noteId
     * @param collectId
     * @param userId

     */
    public boolean changeCollectNoteWithTx(Boolean isCollect, Long noteId, Integer collectId, Integer userId);

    /**
     * 获取普通删除的笔记列表
     *
     * @param userId
     * @return
     
     */
    List<Note> getDeleteNoteList(Integer userId);

    /**
     * 获取用户状态正常的笔记数
     *
     * @param userId
     * @return
     */
    Long getUserNoteCount(Integer userId);

    /**
     * 删除笔记信息（ES和缓存）
     * @param noteId
     * @param userId
     */
    void deleteNoteById(Long noteId, Integer userId);


}