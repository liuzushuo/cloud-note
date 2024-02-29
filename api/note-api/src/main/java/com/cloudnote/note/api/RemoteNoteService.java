package com.cloudnote.note.api;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.ServiceNameConstants;
import com.cloudnote.note.api.factory.RemoteNoteFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 云笔记-笔记服务
 */
@FeignClient(contextId = "remoteNoteService", value = ServiceNameConstants.NOTE_SERVICE, fallbackFactory = RemoteNoteFallbackFactory.class)
public interface RemoteNoteService {

    /**
     * 获取普通删除的笔记列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/list/deleted")
    public R getDeleteNoteList(@RequestParam("userId") Integer userId);

    /**
     * 恢复一个笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    @PutMapping("/restore/one")
    public R restoreOneNote(@RequestParam("noteId") Long noteId, @RequestParam("userId") Integer userId);

    /**
     * 批量恢复笔记
     *
     * @param noteIdList
     * @param userId
     * @return
     */
    @PutMapping("/restore/bunches")
    public R restoreBunchesNote(@RequestBody List<Long> noteIdList, @RequestParam("userId") Integer userId);

    /**
     * 彻底删除一个笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/one")
    public R deleteNoteById(@RequestParam("noteId") Long noteId, @RequestParam("userId") Integer userId);

    /**
     * 批量彻底删除笔记
     *
     * @param noteIdList
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/bunches")
    public R completeDeleteBunchesNote(@RequestBody List<Long> noteIdList, @RequestParam("userId") Integer userId);
}
