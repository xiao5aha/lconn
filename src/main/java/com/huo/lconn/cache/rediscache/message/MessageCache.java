package com.huo.lconn.cache.rediscache.message;

import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/26 17:43
 */
public interface MessageCache {
    /**
     * 向redis里添加一条消息缓存
     * @param realTopic
     * @param message
     * @param expiry 每条消息的过期时间
     */
    void addMessage(String realTopic,Object message,long expiry);

    /**
     * 从redis里取缓存
     * @param realTopic
     * @return
     */
    Set<Object> getMessage(String realTopic);

    Set<byte[]> getMessageBytes(String realTopic);

    /**
     * 检查redis里是否存在该key
     * @param realTopic
     * @return
     */
    boolean checkMessageCached(String realTopic);
}
