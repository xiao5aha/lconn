package com.huo.lconn.constant;

import io.netty.util.AttributeKey;

import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 16:27
 */
public final class Constant {
    private Constant() {
    }

    public static AttributeKey<Boolean> AUTH_FLAG = AttributeKey.valueOf("AUTH_FLAG");
    public static AttributeKey<Integer> PRODUCT_CODE = AttributeKey.valueOf("PRODUCT_CODE");
    public static AttributeKey<Long> START_TIME = AttributeKey.valueOf("START_TIME");
    public static AttributeKey<Long> NEXT_RESUB_TIME = AttributeKey.valueOf("NEXT_RESUB_TIME");
    public static AttributeKey<String> DEVICE_ID = AttributeKey.valueOf("DEVICE_ID");
    public static AttributeKey<Set<String>> GROUP_TOPIC_SET = AttributeKey.valueOf("GROUP_TOPIC_SET");
    public static AttributeKey<Set<String>> MULTI_TOPIC_SET = AttributeKey.valueOf("MULTI_TOPIC_SET");
    public static AttributeKey<Set<String>> SPECIAL_TOPIC_SET = AttributeKey.valueOf("SPECIAL_TOPIC_SET");
}
