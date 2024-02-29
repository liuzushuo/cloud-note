package com.cloudnote.thing.feign;

import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONUtil;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.thing.api.domain.Thing;
import com.cloudnote.thing.service.IThingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ThingServiceFeign {
    @Autowired
    private IThingService thingService;


    /**
     * 获取普通删除的小记列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/list/deleted")
    public R getDeleteThingList(@RequestParam("userId") Integer userId) {
        // 查询小记列表
        List<Thing> thingList = thingService.getDeleteThingList(userId);
        return thingList != null ? R.ok(JSONUtil.toJsonStr(thingList), "获取成功") : R.fail();
    }

    /**
     * 恢复一个小记
     *
     * @param thingId
     * @param userId
     * @return
     */
    @PutMapping("/restore/one")
    public R restoreOneThing(@RequestParam("thingId") Integer thingId, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(thingId)) return R.fail("小记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 恢复小记
        boolean result = thingService.restoreOneThing(thingId, userId);
        return result ? R.ok("恢复成功") : R.fail("恢复失败");
    }

    /**
     * 批量恢复小记
     *
     * @param thingIdList
     * @param userId
     * @return
     */
    @PutMapping("/restore/bunches")
    public R restoreBunchesThing(@RequestBody List<Integer> thingIdList, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(thingIdList)) return R.fail("小记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 批量恢复小记
        thingService.restoreBunchesThing(thingIdList, userId);
        return R.ok(null, "批量恢复成功");
    }

    /**
     * 彻底删除一个小记
     *
     * @param thingId
     * @param userId
     * @return
     */
    @DeleteMapping("/delete/one")
    public R deleteThingById(@RequestParam("thingId") Integer thingId, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(thingId)) return R.fail("小记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 彻底删除小记
        boolean result = thingService.deleteThingById(true, thingId, userId, true);
        return result ? R.ok("删除成功") : R.fail("删除失败");
    }


    /**
     * 批量删除小记
     *
     * @param thingIdList
     * @param userId
     * @return
     */
    @DeleteMapping("/thing/delete/bunches")
    public R completeDeleteBunchesThing(@RequestBody List<Integer> thingIdList, @RequestParam("userId") Integer userId) {
        // 参数校验
        if (Validator.isEmpty(thingIdList)) return R.fail("小记编号错误");
        if (Validator.isEmpty(userId)) return R.fail("用户编号错误");

        // 批量删除小记
        thingService.completeDeleteBunchesThing(thingIdList, userId);
        return R.ok(null, "批量恢复成功");
    }
}
