package com.huo.lconn.node;

import com.alibaba.druid.util.StringUtils;
import com.huo.lconn.cache.localcache.LocalCache;
import com.huo.lconn.cache.rediscache.node.MultiNodeCacheService;
import com.huo.lconn.cache.rediscache.node.SingleNodeCacheService;
import com.huo.lconn.channel.DelegateChannelService;
import com.huo.lconn.channel.entity.DelegateChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/28 15:30
 * @Desc: 节点服务类，该类负责长连接的续订，节点的增删改查等操作
 */
@Log4j2
@Service
public class DelegateNodeService {

    @Value("${configs.resub_time}")
    private Long reSubTime;

    @Value("${configs.node_connect_port}")
    private String connectPort;

    @Autowired
    private SingleNodeCacheService singleNodeCacheService;

    @Autowired
    private MultiNodeCacheService multiNodeCacheService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void resubChannel() {
        long now = System.currentTimeMillis();
        Set<DelegateChannel> channelsToBeExpired = new HashSet<>();
        for (DelegateChannel channel : LocalCache.CHANNEL_SET) {
            if (channel.getReSubTime() < now + reSubTime * 60 * 1000) {
                channelsToBeExpired.add(channel);
            }
        }
        for (DelegateChannel channel : channelsToBeExpired) {
            LocalCache.THREAD_POOL.execute(() -> {
                log.trace("client channel {} resub start", channel);
                for (String realtopic : channel.getSingleTopics()) {
                    log.trace("client channel {} resub special topic {} node {}", channel, realtopic, getMySelf());
                    singleNodeCacheService.addNode(realtopic, getMySelf());
                }
                for (String realtopic : channel.getMultiTopics()) {
                    log.trace("client channel {} resub multi topic {} node {}", channel, realtopic, getMySelf());
                    multiNodeCacheService.addNode(realtopic, getMySelf());
                }
                log.trace("client channel {} resub end", channel);
            });
        }
    }

    public void addSingle(DelegateChannel channel, String realTopic) {
        channel.getSingleTopics().add(realTopic);
        singleNodeCacheService.addNode(realTopic, getMySelf());
    }

    public void addMulti(DelegateChannel channel, String realTopic) {
        channel.getMultiTopics().add(realTopic);
        multiNodeCacheService.addNode(realTopic, getMySelf());
    }

    public Set<String> getSingle(String realTopic) {
        return singleNodeCacheService.getNode(realTopic);
    }

    public Set<String> getMulti(String realTopic) {
        return multiNodeCacheService.getNode(realTopic);
    }

    public void delSingle(DelegateChannel channel, String realTopic) {
        channel.getSingleTopics().remove(realTopic);
        singleNodeCacheService.delNode(realTopic, getMySelf());
    }

    public void delMulti(DelegateChannel channel, String realTopic) {
        channel.getMultiTopics().remove(realTopic);
        multiNodeCacheService.delNode(realTopic, getMySelf());
    }

    public void setNextResubTime(DelegateChannel channel) {
        long next = System.currentTimeMillis() + 600 * 618L;
        if (log.isTraceEnabled()) {
            log.trace("client channel {} set resub time {}", channel, next);
        }
        channel.setReSubTime(next);
    }

    public void closeChannel(DelegateChannel channel) {
        for (String realTopic : channel.getSingleTopics()) {
            singleNodeCacheService.delNode(realTopic, getMySelf());
        }
        for (String realTopic : channel.getMultiTopics()) {
            multiNodeCacheService.delNode(realTopic, getMySelf());
        }
    }

    public String getMySelf() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("未知host异常 {}", e);
        }
        String ip = addr.getHostAddress().toString();
        return ip + connectPort;
    }

    public boolean isMySelf(String node) {
        return StringUtils.equals(node, getMySelf());
    }
}
