package com.cloudnote.collect.api.factory;


import com.cloudnote.collect.api.RemoteCollectService;
import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.common.api.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * 云笔记-笔记服务降级处理
 *
 * @author ruoyi
 */
@Slf4j
public class RemoteCollectFallbackFactory implements FallbackFactory<RemoteCollectService> {
    /**
     * Returns an instance of the fallback appropriate for the given cause.
     *
     * @param cause cause of an exception.
     * @return fallback
     */
    @Override
    public RemoteCollectService create(Throwable cause) {
        log.error("云笔记-笔记服务调用失败:{}", cause.getMessage());
        return new RemoteCollectService() {
            @Override
            public R createDefaultCollect(Integer userId) {
                return R.fail("创建默认收藏夹失败:" + cause.getMessage());
            }

            /**
             * 获取默认收藏夹id
             *
             * @param userId
             
             * @return
             */
            @Override
            public R getDefaultCollectId(Integer userId) {
                return R.fail("查询默认收藏夹失败:" + cause.getMessage());
            }

            /**
             * 修改收藏夹的count值
             *
             * @param collect
             
             * @return
             */
            @Override
            public R updateCollectCount(Collect collect) {
                return R.fail("修改收藏夹笔记数量失败:" + cause.getMessage());
            }
        };
    }
}
