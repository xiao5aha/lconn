package com.huo.lconn.channel;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.cache.localcache.LocalCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/28 14:30
 * @Desc: 长连接服务类
 */
@Log4j2
@Service
public class DelegateChannelService {

    public Set<DelegateChannel> getChannelsByTopic(String realTopic) {
        Set<DelegateChannel> set = LocalCache.TOPIC_CHANNELS_MAP.get(realTopic);
        return set == null ? Collections.emptySet() : set;
    }

    public void channelSubTopic(String realTopic, DelegateChannel channel) {
        log.trace("client channel {} sub topic {}", channel, realTopic);
        channel.getTopics().add(realTopic);
        Set<DelegateChannel> set = LocalCache.TOPIC_CHANNELS_MAP.get(realTopic);
        if (set == null) {
            set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            Set<DelegateChannel> oldSet = LocalCache.TOPIC_CHANNELS_MAP.putIfAbsent(realTopic, set);
            if (oldSet != null) {
                set = oldSet;
            }
        }
        set.add(channel);
    }

    public void channelUnSubTopic(String realTopic, DelegateChannel channel) {
        log.trace("client channel {} unsub topic {}", channel, realTopic);
        channel.getTopics().remove(realTopic);
        Set<DelegateChannel> set = LocalCache.TOPIC_CHANNELS_MAP.get(realTopic);
        if (set != null) {
            set.remove(channel);
            if (set.isEmpty()) {
                LocalCache.TOPIC_CHANNELS_MAP.remove(realTopic, Collections.EMPTY_SET);
            }
        }
    }

    public int writeMessageAndFlush(String realTopic, GeneratedMessageV3 msg) {
        int count = 0;
        log.trace("client channel topic {} write {}", realTopic, TextFormat.shortDebugString(msg));
        for (DelegateChannel channel : getChannelsByTopic(realTopic)) {
            log.trace("client channel {} topic {} write {}", channel, realTopic, TextFormat.shortDebugString(msg));
            channel.writeAndFlush(msg);
            count++;
        }
        log.trace("client channel topic {} write {} count {}", realTopic, TextFormat.shortDebugString(msg), count);
        return count;
    }

    public void regChannel(DelegateChannel channel) {
        LocalCache.CHANNEL_SET.add(channel);
    }

    public void closeChannel(DelegateChannel channel) {
        for (String realTopic : channel.getTopics()) {
            log.debug("close channel {} clean topic {}", channel, realTopic);
            Set<DelegateChannel> set = LocalCache.TOPIC_CHANNELS_MAP.get(realTopic);
            if (set == null) {
                set.remove(channel);
                if (set.isEmpty()) {
                    LocalCache.TOPIC_CHANNELS_MAP.remove(realTopic, Collections.EMPTY_SET);
                }
            }
        }
        LocalCache.CHANNEL_SET.remove(channel);
    }
}
