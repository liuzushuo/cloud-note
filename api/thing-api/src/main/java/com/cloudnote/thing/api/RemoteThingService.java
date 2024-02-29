package com.cloudnote.thing.api;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.ServiceNameConstants;
import com.cloudnote.thing.api.factory.RemoteThingFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 云笔记-笔记服务
 */
@FeignClient(contextId = "remoteThingService", value = ServiceNameConstants.THING_SERVICE, fallbackFactory = RemoteThingFallbackFactory.class)
public interface RemoteThingService {

    /**
     * 获取普通删除的数据记录
     * @param userId
     * @return
     */
    @GetMapping("/list/deleted")
    public R getDeleteThingList(@RequestParam("userId") Integer userId);

    /**
     * 恢复一个小记
     * @param thingId
     * @param userId
     
     * @return
     */
    @PutMapping("/restore/one")
    public R restoreOneThing(@RequestParam("thingId") Integer thingId,@RequestParam("userId") Integer userId);

    /**
     *  批量恢复小记
     * @param thingIdList
     * @param userId
     
     * @return
     */
    @PutMapping("/restore/bunches")
    public R restoreBunchesThing(@RequestBody List<Integer> thingIdList,@RequestParam("userId") Integer userId);

    /**
     * 彻底删除一个小记
     * @param thingId
     * @param userId
     
     * @return
     */
    @DeleteMapping("/delete/one")
    public R deleteThingById(@RequestParam("thingId") Integer thingId,@RequestParam("userId") Integer userId);

    /**
     * 批量删除小记
     * @param thingIdList
     * @param userId
     
     * @return
     */
    @DeleteMapping("/delete/bunches")
    public R completeDeleteBunchesThing(@RequestBody List<Integer> thingIdList,@RequestParam("userId") Integer userId);
}
