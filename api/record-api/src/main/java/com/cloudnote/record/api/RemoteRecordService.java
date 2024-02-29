package com.cloudnote.record.api;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.ServiceNameConstants;
import com.cloudnote.record.api.dto.RecordDto;
import com.cloudnote.record.api.factory.RemoteRecordFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 云笔记-记录服务
 */
@FeignClient(contextId = "remoteRecordService", value = ServiceNameConstants.RECORD_SERVICE, fallbackFactory = RemoteRecordFallbackFactory.class)
public interface RemoteRecordService {

    /**
     * 添加一条操作记录
     *
     * @param recordVO
     * @param isRollBack // 是否回滚
     * @return
     */
    @PostMapping("/add")
    public R addRecord(@RequestBody RecordDto recordVO, @RequestParam("isRollBack") Boolean isRollBack);

    /**
     * 删除一条操作记录
     *
     * @param userId
     * @param recordId
     * @param type
     * @param isRollBack
     * @return
     */
    @DeleteMapping("/delete")
    public R removeRecord(@RequestParam("userId") Integer userId, @RequestParam("recordId") Long recordId, @RequestParam("type") Integer type, @RequestParam("isRollBack") Boolean isRollBack);
}
