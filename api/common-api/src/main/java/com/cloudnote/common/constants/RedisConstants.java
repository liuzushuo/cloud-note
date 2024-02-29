package com.cloudnote.common.constants;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;


    public static final Long CACHE_NULL_TTL = 2L;

    // 笔记
    public static final String CACHE_NOTE_LIST_KEY = "cache:note_list:";
    public static final Long CACHE_NOTE_LIST_TTL = 30L;

    // 小记
    public static final String CACHE_THING_LIST_KEY = "cache:thing_list:";
    public static final Long CACHE_THING_LIST_TTL = 30L;

    // 记录
    public static final String CACHE_RECORD_LIST_KEY = "cache:record_list:";

    // 收藏夹
    public static final String CACHE_COLLECT_LIST_KEY = "cache:collect_list:";
    public static final Long CACHE_COLLECT_LIST_TTL = 30L;
}
