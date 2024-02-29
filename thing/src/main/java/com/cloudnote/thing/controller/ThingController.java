package com.cloudnote.thing.controller;

import cn.hutool.core.lang.Validator;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.thing.api.domain.Thing;
import com.cloudnote.thing.config.UserHolder;
import com.cloudnote.thing.service.IThingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class ThingController {
    @Autowired
    private IThingService thingService;

    /**
     * 获取用户小记列表
     *
     * @param search 查询关键词（标题或标签中含有关键词）
     * @param filter 过滤条件（null:默认，0：未完成，1：已完成）
     * @return
     */
    @GetMapping("/list")
    public R getUserThingList(String search, Integer filter) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询小记列表
        return R.ok(thingService.getUserNormalThing(search, filter, userId));
    }

    /**
     * 置顶小记
     *
     * @param isTop   是否置顶
     * @param thingId 小记id
     * @return
     */
    @GetMapping("/top")
    public R topThing(boolean isTop, Integer thingId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.参数校验
        // 验证置顶参数
        if (Validator.isEmpty(isTop)) return R.fail("置顶参数有误");
        // 验证小记id
        if (Validator.isEmpty(thingId)) return R.fail("小记编号参数有误");
        // 3.修改小记置顶状态
        thingService.topThing(isTop, thingId, userId);
        return R.ok("修改成功");
    }

    /**
     * 根据id删除小记
     *
     * @param complete     是否为彻底删除
     * @param thingId
     * @param isRecycleBin 是否为回收站中的操作
     * @return
     */
    @DeleteMapping("/delete")
    public R deleteThingById(Boolean complete, Integer thingId, Boolean isRecycleBin) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.参数校验
        // 验证彻底删除参数
        if (Validator.isEmpty(complete))
            return R.fail("删除参数有误");
        // 验证是否为回收站操作参数
        if (Validator.isEmpty(isRecycleBin))
            return R.fail("删除参数有误");
        // 验证小记id
        if (Validator.isEmpty(thingId)) return R.fail("小记编号参数有误");
        // 3.删除小记
        boolean result = thingService.deleteThingById(complete, thingId, userId, isRecycleBin);
        return result ? R.ok("删除成功") : R.fail("删除小记失败");
    }

    /**
     * 新增小记
     *
     * @param title    标题
     * @param top      是否置顶
     * @param tags     标签（"六一,礼物,儿童节"）
     * @param content  内容（"[{"checked":true,"thing":"气球"},{"checked":true,"thing":"棒棒糖"}]"）
     * @param finished 是否完成
     * @return
     */
    @PostMapping("/create")
    public R createThing(String title, boolean top, String tags, String content, boolean finished) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.参数校验
        // 验证标题
        if (Validator.isEmpty(title))
            return R.fail("小记标题参数有误");
        // 验证是否置顶
        if (Validator.isEmpty(top))
            return R.fail("小记置顶参数有误");
        // 验证标签
        if (Validator.isEmpty(tags)) return R.fail("小记标签参数有误");
        // 验证内容
        if (Validator.isEmpty(content))
            return R.fail("小记内容参数有误");
        // 验证是否完成
        if (Validator.isEmpty(content))
            return R.fail("小记完成参数有误");


        Thing thing = Thing.builder()
                .title(title)
                .top(top)
                .tags(tags)
                .content(content)
                .finished(finished)
                .userId(userId)
                .build();
        // 2.新增小记
        thingService.createThing(thing);
        return R.ok("新增小记成功");
    }

    /**
     * 获取编辑的小记信息
     *
     * @param thingId
     * @return
     */
    @GetMapping("/edit")
    public R getEditThing(@RequestParam("thingId") Integer thingId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证小记id
        if (Validator.isEmpty(thingId)) return R.fail("小记编号参数有误");

        // 2.查询小记
        Thing thing = thingService.getThingById(thingId, userId);
        return thing != null ? R.ok(thing,"获取成功") : R.fail("获取小记信息失败");
    }

    /**
     * 更新小记
     *
     * @param thingId
     * @param title
     * @param top
     * @param tags
     * @param content
     * @param finished
     * @return
     */
    @PutMapping("/update")
    public R updateThing(Integer thingId, String title, boolean top, String tags, String content, boolean finished) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证小记id
        if (Validator.isEmpty(thingId)) return R.fail("小记编号参数有误");
        // 验证标题
        if (Validator.isEmpty(title))
            return R.fail("小记标题参数有误");
        // 验证是否置顶
        if (Validator.isEmpty(top))
            return R.fail("小记置顶参数有误");
        // 验证标签
        if (Validator.isEmpty(tags)) return R.fail("小记标签参数有误");
        // 验证内容
        if (Validator.isEmpty(content))
            return R.fail("小记内容参数有误");
        // 验证是否完成
        if (Validator.isEmpty(content))
            return R.fail("小记完成参数有误");

        Thing thing = Thing.builder()
                .id(thingId)
                .title(title)
                .top(top)
                .tags(tags)
                .content(content)
                .finished(finished)
                .userId(userId)
                .build();
        // 2.更新小记
        boolean result = thingService.updateThing(thing);
        return result ? R.ok("更新成功") : R.fail("更新失败");
    }
}
