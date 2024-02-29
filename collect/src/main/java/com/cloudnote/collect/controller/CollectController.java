package com.cloudnote.collect.controller;

import cn.hutool.core.lang.Validator;
import com.cloudnote.collect.api.domain.Collect;
import com.cloudnote.collect.config.UserHolder;
import com.cloudnote.collect.service.ICollectService;
import com.cloudnote.common.api.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CollectController {
    @Autowired
    private ICollectService collectService;

    @GetMapping("/list")
    public R getNormalCollectList() {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询历史记录
        return R.ok(collectService.getNormalCollectList(userId));
    }

    /**
     * 创建收藏夹
     *
     * @param name
     * @return
     */
    @PostMapping("/create")
    public R createCollect(String name) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证名称
        if (Validator.isEmpty(name))
            return R.fail("收藏夹名称参数有误");
        Collect collect = Collect.builder()
                .name(name)
                .count(0)
                .isDefault(false)
                .userId(userId)
                .build();
        // 2.新增收藏夹
        boolean result = collectService.createCollect(collect);
        return result ? R.ok("新增收藏夹成功") : R.fail("新增收藏夹失败");
    }

    /**
     * 查询要编辑的收藏夹信息
     *
     * @param collectId
     * @return
     */
    @GetMapping("/edit")
    public R getEditCollect(Integer collectId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证收藏夹id
        if (Validator.isEmpty(collectId)) return R.fail("收藏夹编号参数有误");
        // 2.查询收藏夹
        Collect collect = collectService.getCollectById(collectId, userId);
        return collect != null ? R.ok(collect,"获取成功") : R.fail("收藏夹不存在");
    }

    /**
     * 修改收藏夹名称
     *
     * @param collectId
     * @param name
     * @return
     */
    @PutMapping("/update")
    public R updateCollectName(Integer collectId, String name) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证名称
        if (Validator.isEmpty(name))
            return R.fail("收藏夹名称参数有误");

        Collect collect = Collect.builder()
                .id(collectId)
                .name(name)
                .userId(userId)
                .build();
        // 2.修改收藏夹
        return collectService.updateCollect(collect) ? R.ok("修改收藏夹成功") : R.fail("修改收藏夹失败");
    }

    /**
     * 删除收藏夹
     *
     * @param collectId
     * @return
     */
    @DeleteMapping("/delete")
    public R deleteCollect(Integer collectId) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 验证收藏夹id
        if (Validator.isEmpty(collectId)) return R.fail("收藏夹编号参数有误");

        // 2.删除收藏夹
        return collectService.deleteCollect(collectId, userId) ? R.ok("删除成功") : R.fail("删除失败");
    }
}
