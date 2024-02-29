package com.cloudnote.thing.api.factory;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.thing.api.RemoteThingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * 云笔记-笔记服务降级处理
 *
 * @author ruoyi
 */
@Slf4j
public class RemoteThingFallbackFactory implements FallbackFactory<RemoteThingService> {
    /**
     * Returns an instance of the fallback appropriate for the given cause.
     *
     * @param cause cause of an exception.
     * @return fallback
     */
    @Override
    public RemoteThingService create(Throwable cause) {
        log.error("云笔记-小记服务调用失败:{}", cause.getMessage());
        return new RemoteThingService() {

            /**
             * 获取普通删除的小记列表
             *
             * @param userId
             * @return
             */
            @Override
            public R getDeleteThingList(Integer userId) {
                return R.fail("获取普通删除的小记列表失败:" + cause.getMessage());
            }

            /**
             * 恢复一个小记
             *
             * @param thingId
             * @param userId
             * @return
             */
            @Override
            public R restoreOneThing(Integer thingId, Integer userId) {
                return R.fail("恢复小记失败:" + cause.getMessage());
            }

            /**
             * 批量恢复小记
             *
             * @param thingIdList
             * @param userId
             * @return
             */
            @Override
            public R restoreBunchesThing(List<Integer> thingIdList, Integer userId) {
                return R.fail("批量恢复小记失败:" + cause.getMessage());
            }

            /**
             * 彻底删除一个小记
             *
             * @param thingId
             * @param userId
             * @return
             */
            @Override
            public R deleteThingById(Integer thingId, Integer userId) {
                return R.fail("彻底删除小记失败:" + cause.getMessage());
            }

            /**
             * 批量删除小记
             *
             * @param thingIdList
             * @param userId
             * @return
             */
            @Override
            public R completeDeleteBunchesThing(List<Integer> thingIdList, Integer userId) {
                return R.fail("批量删除小记失败:" + cause.getMessage());
            }
        };
    }
}
