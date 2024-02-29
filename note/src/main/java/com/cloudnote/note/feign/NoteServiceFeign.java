package com.cloudnote.note.feign;

import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.note.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class NoteServiceFeign {
    @Autowired
    private INoteService noteService;


    /**
     * 获取普通删除的笔记列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/list/deleted")
    public R getDeleteNoteList(@RequestParam("userId") Integer userId) {
        // 查询笔记列表
        List<Note> noteList = noteService.getDeleteNoteList(userId);
        return noteList != null ? R.ok(JSONUtil.toJsonStr(noteList),"获取成功") : R.fail();
    }

    /**
     * 恢复一个笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    @PutMapping("/restore/one")
    public R restoreOneNote(@RequestParam("noteId") Long noteId, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 恢复笔记
        boolean result = noteService.restoreOneNote(noteId, userId);
        return result ? R.ok() : R.fail("恢复笔记失败");
    }

    /**
     * 批量恢复笔记
     *
     * @param noteIdList
     * @param userId
     * @return
     */
    @PutMapping("/restore/bunches")
    public R restoreBunchesNote(@RequestBody List<Long> noteIdList, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(noteIdList)) return R.fail("笔记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 批量恢复笔记
        noteService.restoreBunchesNote(noteIdList, userId);
        return R.ok(null, "批量恢复成功");
    }

    /**
     * 彻底删除一个笔记
     *
     * @param noteId
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/one")
    public R deleteNoteById(@RequestParam("noteId") Long noteId, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 彻底删除笔记
        boolean result = noteService.deleteNote(true, noteId, userId, true);
        return result ? R.ok() : R.fail("彻底删除笔记失败");
    }

    /**
     * 批量彻底删除笔记
     *
     * @param noteIdList
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/bunches")
    public R completeDeleteBunchesNote(@RequestBody List<Long> noteIdList, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(noteIdList)) return R.fail("笔记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 批量删除笔记
        noteService.completeDeleteBunchesNote(noteIdList, userId);
        return R.ok(null, "批量删除成功");
    }
}
