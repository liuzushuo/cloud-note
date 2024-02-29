package com.cloudnote.collect.api;


import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.collect.api.factory.RemoteCollectFallbackFactory;
import com.cloudnote.common.api.dto.R;
import com.cloudnote.common.constants.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 云笔记-收藏服务
 */
@FeignClient(contextId = "remoteCollectService", value = ServiceNameConstants.COLLECT_SERVICE, fallbackFactory = RemoteCollectFallbackFactory.class)
public interface RemoteCollectService {
    /**
     * 创建默认收藏夹
     *
     * @return
     */
    @PostMapping("/init")
    public R createDefaultCollect(@RequestParam("userId") Integer userId);

    /**
     * 获取默认收藏夹id
     *
     * @return
     */
    @GetMapping("/default")
    public R getDefaultCollectId(@RequestParam("userId") Integer userId);

    /**
     * 修改收藏夹的count值
     *
     * @param collect
     * @return
     */
    @PutMapping("/update/count")
    public R updateCollectCount(@RequestBody Collect collect);
}
