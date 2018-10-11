package com.huo.lconn.cache.rediscache.node;

import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/26 20:24
 */
public interface NodeCache {

    static final String NODE_PREFIX = "node:";

    /**
     * 添加节点
     * @param realTopic
     * @param node
     */
    void addNode(String realTopic, String node);

    /**
     * 获取节点
     * @param realTopic
     * @return
     */
    Set<String> getNode(String realTopic);

    /**
     * 删除节点
     * @param realTopic
     * @param node
     */
    void delNode(String realTopic,String node);
}
