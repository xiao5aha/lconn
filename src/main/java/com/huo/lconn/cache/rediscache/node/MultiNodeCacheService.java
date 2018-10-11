package com.huo.lconn.cache.rediscache.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/26 20:27
 * @Desc: 一个主题多个节点的一对多关系缓存类
 */
@Service
public class MultiNodeCacheService implements NodeCache {

    @Value("${configs.node-expire}")
    private Long nodeExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void addNode(String realTopic, String node) {
        //主题对应节点使用redis缓存，默认过期时间为10分钟
        redisTemplate.opsForZSet().add(NodeCache.NODE_PREFIX + realTopic, node, System.currentTimeMillis() + nodeExpire * 60 * 1000);
        redisTemplate.expire(NodeCache.NODE_PREFIX + realTopic, nodeExpire, TimeUnit.MINUTES);
        redisTemplate.opsForZSet().removeRangeByScore(NodeCache.NODE_PREFIX + realTopic, 0, System.currentTimeMillis());
    }

    @Override
    public Set<String> getNode(String realTopic) {
        return redisTemplate.opsForZSet().rangeByScore(NodeCache.NODE_PREFIX + realTopic, 0, Double.MAX_VALUE);
    }

    @Override
    public void delNode(String realTopic, String node) {
        redisTemplate.opsForZSet().remove(NodeCache.NODE_PREFIX + realTopic, node);
    }
}
