package com.huo.lconn.cache.rediscache.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/26 17:46
 */
@Service
public class MessageCacheService implements MessageCache {

    private static final String KEY_PREFIX = "message:";

    @Value("${configs.topic-expire}")
    private Long topicExpire;

    @Value("${configs.message-expire}")
    private Long messageExpire;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplateForByte;

    @Override
    public void addMessage(String realTopic, Object message, long expiry) {
        //消息过期时间最长为1天，超过一天的按照一天计算
        expiry = Math.min(expiry,System.currentTimeMillis() + messageExpire*24*60*60*1000);
        redisTemplate.opsForZSet().add(KEY_PREFIX + realTopic, message, expiry);
        redisTemplate.expire(KEY_PREFIX + realTopic, topicExpire, TimeUnit.DAYS);
        //由于zset中的消息没有过期时间，所以需要手动置为过期
        redisTemplate.opsForZSet().removeRangeByScore(KEY_PREFIX + realTopic, 0,System.currentTimeMillis());
    }

    @Override
    public Set<Object> getMessage(String realTopic) {
        //获取尚未过期的那些消息,获取到之后删除缓存
        Set<Object> messages = redisTemplate.opsForZSet().rangeByScore(KEY_PREFIX + realTopic, System.currentTimeMillis(),Double.MAX_VALUE);
        redisTemplate.delete(KEY_PREFIX + realTopic);
        return messages;
    }

    @Override
    public Set<byte[]> getMessageBytes(String realTopic) {
        //获取尚未过期的那些消息,获取到之后删除缓存
        Set<byte[]> messages = redisTemplateForByte.opsForZSet().rangeByScore(KEY_PREFIX + realTopic, System.currentTimeMillis(),Double.MAX_VALUE);
        redisTemplate.delete(KEY_PREFIX + realTopic);
        return messages;
    }

    @Override
    public boolean checkMessageCached(String realTopic) {
        return redisTemplate.hasKey(KEY_PREFIX + realTopic);
    }
}
