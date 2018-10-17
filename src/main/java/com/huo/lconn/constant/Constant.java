package com.huo.lconn.constant;

import io.netty.util.AttributeKey;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 16:27
 */
public final class Constant {
    public static final int DEFAULT_RPC_PORT = 7023;
    public static final int HEART_TIME_OUT = 20;
    public static final String ZK_PATH = "/nettypush/rpc";
    public static final int NETTY_BACKENT_PORT = 7071;
    public static final int NETTY_CLIENT_PORT = 7020;
    public static final String ZK_ROOT = "/nettypush_clientServer";
    public static final String ZK_HOST_PARENT = "/host";
    public static final String ZK_NODE_PARENT = "/node";
    public static final String SOCKETIO_EVENT = "data";
    public static final int NETTY_WORKER_NUM = 4;
    public static final int OTHER_WORKER_NUM = 30;
    public static AttributeKey<Boolean> AUTH_FLAG = AttributeKey.valueOf("AUTH_FLAG");
    public static AttributeKey<Integer> PRODUCT_CODE = AttributeKey.valueOf("PRODUCT_CODE");
    public static AttributeKey<Long> START_TIME = AttributeKey.valueOf("START_TIME");
    public static AttributeKey<Long> NEXT_RESUB_TIME = AttributeKey.valueOf("NEXT_RESUB_TIME");
    public static AttributeKey<String> DEVICE_ID = AttributeKey.valueOf("DEVICE_ID");
    public static AttributeKey<Set<String>> GROUP_TOPIC_SET = AttributeKey.valueOf("GROUP_TOPIC_SET");
    public static AttributeKey<Set<String>> MULTI_TOPIC_SET = AttributeKey.valueOf("MULTI_TOPIC_SET");
    public static AttributeKey<Set<String>> SPECIAL_TOPIC_SET = AttributeKey.valueOf("SPECIAL_TOPIC_SET");
    public static final String BLACK_LIST = "black_list";
}
