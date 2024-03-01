package com.cloudnote.search.service;


import java.util.Map;

/**
 * 查询业务的接口
 */
public interface ISearchService {
    /**
     * 关键字查询
     * @param keyword
     * @param userId
     * @return
     */
    public Map searchByKeyword(String keyword, Integer userId);
}
