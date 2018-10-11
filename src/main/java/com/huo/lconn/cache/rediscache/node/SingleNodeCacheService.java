package com.huo.lconn.cache.rediscache.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/27 10:59
 * @Desc: 一个主题一个节点的一一对应关系缓存类
 */
@Service
public class SingleNodeCacheService implements NodeCache {

    @Value("${configs.node-expire}")
    private Long nodeExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void addNode(String realTopic, String node) {
        redisTemplate.opsForValue().set(NodeCache.NODE_PREFIX + realTopic, node, nodeExpire, TimeUnit.MINUTES);
    }

    @Override
    public Set<String> getNode(String realTopic) {
         String result = redisTemplate.opsForValue().get(NodeCache.NODE_PREFIX + realTopic);
         if(result==null) return Collections.emptySet();
         else return Collections.singleton(result);
    }

    @Override
    public void delNode(String realTopic, String node) {
        redisTemplate.delete(NodeCache.NODE_PREFIX + realTopic);
    }
}
