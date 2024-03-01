package com.cloudnote.search.controller;


import com.cloudnote.common.api.dto.R;
import com.cloudnote.search.config.UserHolder;
import com.cloudnote.search.service.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ISearchService searchService;

    /**
     * 关键词搜索
     *
     * @param keyword
     * @return
     */
    @GetMapping
    public R searchByKeyword(@RequestParam(value = "keyword", required = false) String keyword) {
        // 1.获取用户id
        Integer userId = UserHolder.getUser().getId();
        // 2.查询
        Map resultMap = searchService.searchByKeyword(keyword, userId);
        return R.ok(resultMap, "获取成功");
    }
}
