package com.cloudnote.search.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloudnote.note.api.RemoteNoteService;
import com.cloudnote.note.api.domain.Note;
import com.cloudnote.search.service.ISearchService;
import com.cloudnote.thing.api.RemoteThingService;
import com.cloudnote.thing.api.domain.Thing;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {
    @Autowired
    private RemoteNoteService noteService;
    @Autowired
    private RemoteThingService thingService;
    @Autowired
    private RestHighLevelClient client;

    /**
     * 关键字查询
     *
     * @param keyword
     * @param userId
     * @return
     */
    @Override
    public Map searchByKeyword(String keyword, Integer userId) {
        // 1.查询笔记
        List<Note> noteList = searchNotesByKeyword(keyword, userId);
        // 2.查询小记
        List<Thing> thingList = searchThingsByKeyword(keyword, userId);
        // 3.封装返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("noteList", noteList);
        resultMap.put("thingList", thingList);
        return resultMap;
    }

    /**
     * 根据关键词搜索笔记列表
     *
     * @param keyword
     * @param userId
     * @return
     */
    private List<Note> searchNotesByKeyword(String keyword, Integer userId) {
        // 1.创建请求
        SearchRequest request = new SearchRequest("note");
        // 2.准备DSL
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StrUtil.isNotEmpty(keyword)) {
            boolQuery.must(QueryBuilders.matchQuery("all", keyword));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        boolQuery.filter(QueryBuilders.termQuery("userId", userId));
        boolQuery.filter(QueryBuilders.termQuery("status", 1));
        request.source().query(boolQuery);
        if (StrUtil.isEmpty(keyword)) {
            // 如果无关键字，则默认查询最近操作过的四条记录
            request.source().from(0).size(4);
            request.source().sort("updateTime", SortOrder.DESC);
        }
        // 3.发送请求
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("查询笔记列表异常");
        }
        // 4.解析数据
        if (response.status().getStatus() != 200) return null;
        List<Note> noteList = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            Note note = JSONUtil.toBean(json, Note.class);
            Note result = new Note();
            result.setId(note.getId());
            result.setTitle(note.getTitle());
            noteList.add(result);
        }
        return noteList;
    }

    /**
     * 根据关键词查询小记
     *
     * @param keyword
     * @param userId
     * @return
     */
    private List<Thing> searchThingsByKeyword(String keyword, Integer userId) {
        // 1.创建请求
        SearchRequest request = new SearchRequest("thing");
        // 2.准备DSL
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StrUtil.isNotEmpty(keyword)) {
            boolQuery.must(QueryBuilders.matchQuery("all", keyword));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        boolQuery.filter(QueryBuilders.termQuery("userId", userId));
        boolQuery.filter(QueryBuilders.termQuery("status", 1));
        request.source().query(boolQuery);
        if (StrUtil.isEmpty(keyword)) {
            // 如果无关键字，则默认查询最近操作过的四条记录
            request.source().from(0).size(4);
            request.source().sort("updateTime", SortOrder.DESC);
        }
        // 3.发送请求
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("查询小记列表异常");
        }
        // 4.解析数据
        if (response.status().getStatus() != 200) return null;
        List<Thing> thingList = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            Thing thing = JSONUtil.toBean(json, Thing.class);
            Thing result = new Thing();
            result.setId(thing.getId());
            result.setTitle(thing.getTitle());
            thingList.add(result);
        }
        return thingList;
    }
}
