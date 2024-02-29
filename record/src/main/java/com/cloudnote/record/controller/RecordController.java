package com.cloudnote.record.controller;


import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.record.api.dto.RecordDto;
import com.cloudnote.record.config.UserHolder;
import com.cloudnote.record.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class RecordController {

    @Autowired
    private IRecordService recordService;

    /**
     * 获取历史记录
     *
     * @return
     */
    @GetMapping("/list")
    public R getRecords() {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询历史记录
        List<RecordDto> records = recordService.getRecords(userId);
        return R.ok(records, "获取成功");
    }

    /**
     * 获取普通删除的数据（笔记和小记）
     *
     * @return
     */
    @GetMapping("/deleteList")
    public R getDeleteList() {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询历史记录
        List<RecordDto> records = recordService.getDeleteList(userId);
        return R.ok(records, "获取成功");
    }

    /**
     * 恢复一条被删除的数据
     *
     * @param recoreId
     * @param type
     * @return
     */
    @PostMapping("/restore/one")
    public R restoreOne(Long recoreId, Integer type) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 校验数据id
        if (Validator.isEmpty(recoreId)) return R.fail("数据编号参数有误");
        // 校验类型
        if (Validator.isEmpty(type)) return R.fail("数据类型参数有误");

        // 2.恢复数据
        boolean result = recordService.restoreOne(recoreId, type, userId);
        return result ? R.ok("恢复成功") : R.fail("恢复失败");
    }


    /**
     * 批量恢复被删除的数据
     *
     * @param bunches 元素格式：id:type
     * @return
     */
    @PostMapping("/restore/bunches")
    public R restoreBunches(String bunches) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 校验数据标识
        if (Validator.isEmpty(bunches)) return R.fail("数据编号参数有误");
        List<String> list = JSONUtil.toList(bunches, String.class);
        // 2.恢复数据
        recordService.restoreBunches(list, userId);
        return R.ok("恢复成功");
    }

    /**
     * 彻底删除一条数据
     *
     * @param recoreId
     * @param type
     * @return
     */
    @DeleteMapping("/delete/one")
    public R completeDeleteOne(Long recoreId, Integer type) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.校验参数
        // 校验数据id
        if (Validator.isEmpty(recoreId)) return R.fail("数据编号参数有误");
        // 校验类型
        if (Validator.isEmpty(type)) return R.fail("数据类型参数有误");

        // 2.彻底删除数据
        boolean result = recordService.completeDeleteOne(recoreId, type, userId);
        return result ? R.ok("删除成功") : R.fail("删除失败");
    }

    /**
     * 批量彻底删除数据
     *
     * @param bunches
     * @return
     */
    @DeleteMapping("/delete/bunches")
    public R completeDeleteBunches(String bunches) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 解码
        try {
            bunches = URLDecoder.decode(bunches, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return R.fail("数据编号参数有误");
        }
        // 校验数据标识
        if (Validator.isEmpty(bunches)) return R.fail("数据编号参数有误");
        List<String> list = JSONUtil.toList(bunches, String.class);
        // 2.批量彻底删除数据
        recordService.completeDeleteBunches(list, userId);
        return R.ok("彻底删除成功");
    }

}
