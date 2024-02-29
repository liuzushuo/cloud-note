package com.cloudnote.note.api.factory;

import com.cloudnote.common.api.dto.R;
import com.cloudnote.note.api.RemoteNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

/**
 * 云笔记-笔记服务降级处理
 *
 * @author ruoyi
 */
@Slf4j
public class RemoteNoteFallbackFactory implements FallbackFactory<RemoteNoteService> {
    /**
     * Returns an instance of the fallback appropriate for the given cause.
     *
     * @param cause cause of an exception.
     * @return fallback
     */
    @Override
    public RemoteNoteService create(Throwable cause) {
        log.error("云笔记-笔记服务调用失败:{}", cause.getMessage());
        return new RemoteNoteService() {

            /**
             * 获取普通删除的笔记列表
             *
             * @param userId
             * @return
             */
            @Override
            public R getDeleteNoteList(Integer userId) {
                return R.fail("获取普通删除的笔记列表失败:" + cause.getMessage());
            }

            /**
             * 恢复一个笔记
             *
             * @param noteId
             * @param userId
             * @return
             */
            @Override
            public R restoreOneNote(Long noteId, Integer userId) {
                return R.fail("恢复笔记失败:" + cause.getMessage());
            }

            /**
             * 批量恢复笔记
             *
             * @param noteIdList
             * @param userId
             * @return
             */
            @Override
            public R restoreBunchesNote(List<Long> noteIdList, Integer userId) {
                return R.fail("批量恢复笔记失败:" + cause.getMessage());
            }

            /**
             * 彻底删除一个笔记
             *
             * @param noteId
             * @param userId
             * @return
             */
            @Override
            public R deleteNoteById(Long noteId, Integer userId) {
                return R.fail("彻底删除笔记失败:" + cause.getMessage());
            }

            /**
             * 批量彻底删除笔记
             *
             * @param noteIdList
             * @param userId
             * @return
             */
            @Override
            public R completeDeleteBunchesNote(List<Long> noteIdList, Integer userId) {
                return R.fail("批量删除笔记失败:" + cause.getMessage());
            }
        };
    }
}
