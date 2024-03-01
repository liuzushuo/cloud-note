package com.cloudnote.common.constants;

public class MqConstants {
    // 笔记
    public final static String NOTE_INSERT_TOPIC = "note_insert_topic";
    public final static String NOTE_RESTOE_OR_DELETE_TX_TOPIC = "note_restore_or_delete_tx_topic";
    public final static String NOTE_COLLECT_TX_TOPIC = "note_collect_tx_topic";
    public final static String NOTE_DELETE_TX_GROUP = "CN_NOTE_DELETE_TX_PRODUCER_GROUP";
    public final static String NOTE_RESTORE_TX_GROUP = "CN_NOTE_RESTORE_TX_PRODUCER_GROUP";
    public final static String NOTE_COLLECT_TX_GROUP = "CN_NOTE_COLLECT_TX_PRODUCER_GROUP";

    // 小记
    public final static String THING_INSERT_TOPIC = "thing_insert_topic";
    public final static String THING_DELETE_TOPIC = "thing_delete_topic";

    // 收藏夹
    public final static String COLLECT_INSERT_TOPIC = "collect_insert_topic";
    public final static String COLLECT_DELETE_TOPIC = "collect_delete_topic";

    // 用户
    public final static String USER_COLLECT_TX_GROUP = "CN_USER_COLLECT_TX_PRODUCER_GROUP";
    public final static String USER_COLLECT_TX_TOPIC = "user_collect_tx_topic";

    // 订单
    public final static String PAY_STATUS_SAVE_TX_GROUP = "CN_PAY_STATUS_SAVE_TX_PRODUCER_GROUP";
    public final static String PAY_STATUS_SAVE_TX_TOPIC = "pay_status_tx_topic";

}