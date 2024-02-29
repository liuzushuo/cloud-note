package com.cloudnote.note.controller;

import cn.hutool.core.lang.Validator;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.note.api.dto.NoteDTO;
import com.cloudnote.note.config.UserHolder;
import com.cloudnote.note.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
public class NoteController {
    @Autowired
    private INoteService noteService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取用户笔记列表
     *
     * @return
     */
    @GetMapping("/list")
    public R getUserNoteList() {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询笔记列表
        return R.ok(noteService.getUserNormalNotes(userId));
    }

    /**
     * 查询用户笔记数
     *
     * @return
     */
    @GetMapping("/count")
    public R getUserNoteCount() {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询用户笔记数
        return R.ok(noteService.getUserNoteCount(userId));
    }

    /**
     * 置顶笔记
     *
     * @param isTop  是否置顶
     * @param noteId 笔记id
     * @return
     */
    @GetMapping("/top")
    public R topThing(boolean isTop, Long noteId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 验证置顶参数
        if (Validator.isEmpty(isTop)) return R.fail("置顶参数有误");
        // 验证笔记id
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号参数有误");
        // 3.修改小记置顶状态
        noteService.topNote(isTop, noteId, userId);
        return R.ok("修改成功");
    }

    /**
     * 根据id删除笔记
     *
     * @param complete     是否为彻底删除
     * @param noteId
     * @param isRecycleBin 是否为回收站中的操作
     * @return
     */
    @DeleteMapping("/delete")
    public R deleteNoteById(Boolean complete, Long noteId, Boolean isRecycleBin) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 验证彻底删除参数
        if (Validator.isEmpty(complete))
            return R.fail("删除参数有误");
        // 验证是否为回收站操作参数
        if (Validator.isEmpty(isRecycleBin))
            return R.fail("删除参数有误");
        // 验证笔记id
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号参数有误");

        // 3.删除笔记
        boolean result = noteService.deleteNote(complete, noteId, userId, isRecycleBin);
        return result ? R.ok("删除成功") : R.fail("删除笔记失败");
    }

    /**
     * 新增笔记
     *
     * @param level 是否为会员（0：普通用户，1：高级会员）
     * @return
     */
    @PostMapping("/create")
    public R createNote(Integer level) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.创建笔记
        Long noteId = noteService.createNoteInit(level, userId);
        return noteId != null ? R.ok(noteId,"创建成功") : R.fail("创建笔记失败");
    }

    /**
     * 获取编辑的笔记信息
     *
     * @param noteId
     * @return
     */
    @GetMapping("/edit")
    public R getEditNote(Long noteId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.验证笔记id
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号参数有误");

        // 3.查询笔记
        NoteDTO noteDTO = noteService.getNoteById(noteId, userId);
        return noteDTO != null ? R.ok(noteDTO,"获取成功") : R.fail("获取笔记失败");
    }

    /**
     * 保存笔记
     *
     * @param noteId
     * @param title  笔记标题
     * @param body   笔记内容
     * @return
     */
    @PostMapping("/save")
    public R saveEditingNote(Long noteId, String title, String body) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.验证笔记id
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号参数有误");

        // 2.保存笔记
        Date localTime = noteService.saveEditingNote(noteId, userId, title, body);
        return R.ok(localTime, "保存成功");
    }

    /**
     * 获取收藏夹中收藏笔记列表
     *
     * @param collectId
     * @return
     */
    @GetMapping("/collectList")
    public R getCollectNoteList(Integer collectId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();

        // 2.验证收藏夹id
        if (Validator.isEmpty(collectId)) return R.fail("收藏夹编号参数有误");

        // 3.查询笔记列表
        List<Note> noteList = noteService.getCollectNoteList(collectId, userId);
        return noteList != null ? R.ok(noteList) : R.fail("获取笔记列表失败");
    }


    /**
     * 修改笔记收藏状态
     *
     * @param isCollect 是否收藏
     * @param noteId
     * @param collectId
     * @return
     */
    @PutMapping("/collect")
    public R changeCollectNote(Boolean isCollect, Long noteId, Integer collectId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 验证收藏参数
        if (Validator.isEmpty(isCollect)) return R.fail("置顶参数有误");
        // 验证笔记id
        if (Validator.isEmpty(noteId)) return R.fail("笔记编号参数有误");
        // 验证收藏夹id
        if (Validator.isEmpty(collectId)) return R.fail("收藏夹编号参数有误");

        // 2.修改笔记收藏状态
        boolean result = noteService.changeCollectNote(isCollect, noteId, collectId, userId);
        return result? R.ok("修改成功") : R.fail("修改笔记收藏状态失败");
    }
}
