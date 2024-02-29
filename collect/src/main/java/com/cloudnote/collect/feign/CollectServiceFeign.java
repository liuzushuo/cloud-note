package com.cloudnote.collect.feign;


import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.collect.service.ICollectService;
import com.cloudnote.common.api.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class CollectServiceFeign {
    @Autowired
    private ICollectService collectService;

    /**
     * 创建默认收藏夹
     *
     * @return
     */
    @PostMapping("/init")
    public R createDefaultCollect(@RequestParam("userId") Integer userId) {
        // 创建默认收藏夹
        collectService.createDefaultCollect(userId);
        return R.ok("创建成功");
    }

    /**
     * 查询默认收藏夹id
     *
     * @return
     */
    @GetMapping("/default")
    public R getDefaultCollectId(@RequestParam("userId") Integer userId) {
        // 查询默认收藏夹id
        Integer collectId = collectService.getDefaultCollectId(userId);
        return collectId != null ? R.ok(collectId) : R.fail("查询默认收藏夹失败");
    }

    /**
     * 修改收藏夹的count值
     *
     * @param collect
     * @return
     */
    @PutMapping("/update/count")
    public R updateCollectCount(@RequestBody Collect collect) {
        // 1.参数校验
        if (collect == null || collect.getId() == null || collect.getCount() == null)
            return R.fail("收藏夹参数错误");
        // 2.修改收藏夹
        boolean result = collectService.updateCollect(collect);
        return result ? R.ok("修改成功") : R.fail("修改失败");
    }
}
