package com.cloudnote.record.api.factory;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.record.api.RemoteRecordService;
import com.cloudnote.record.api.dto.RecordDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * 云笔记-笔记服务降级处理
 *
 * @author ruoyi
 */
@Slf4j
public class RemoteRecordFallbackFactory implements FallbackFactory<RemoteRecordService> {
    /**
     * Returns an instance of the fallback appropriate for the given cause.
     *
     * @param cause cause of an exception.
     * @return fallback
     */
    @Override
    public RemoteRecordService create(Throwable cause) {
        log.error("云笔记-记录服务调用失败:{}", cause.getMessage());
        return new RemoteRecordService() {


            /**
             * 添加一条操作记录
             *
             * @param recordVO
             * @param isRollBack // 是否回滚
             * @return
             */
            @Override
            public R addRecord(RecordDto recordVO, Boolean isRollBack) {
                return R.fail("添加操作记录失败:" + cause.getMessage());
            }

            /**
             * 删除一条操作记录
             *
             * @param userId
             * @param recordId
             * @param type
             * @param isRollBack
             * @return
             */
            @Override
            public R removeRecord(Integer userId, Long recordId, Integer type, Boolean isRollBack) {
                return R.fail("删除操作记录失败:" + cause.getMessage());
            }
        };
    }
}
