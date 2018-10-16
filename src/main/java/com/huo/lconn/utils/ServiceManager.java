package com.huo.lconn.utils;

import com.huo.lconn.cache.rediscache.blacklist.BlackListService;
import com.huo.lconn.cache.rediscache.message.MessageCacheService;
import com.huo.lconn.channel.DelegateChannelService;
import com.huo.lconn.node.DelegateNodeService;
import com.huo.lconn.topic.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 14:45
 * @Desc: 工具类，在Runnable的任务类中无法注入，所以使用工具类注入。
 */
@Component
public class ServiceManager {

    public static DelegateChannelService delegateChannelService;
    public static DelegateNodeService delegateNodeService;
    public static TopicService topicService;
    public static MessageCacheService messageCacheService;
    public static BlackListService blackListService;

    @Autowired
    public void setDelegateChannelService(DelegateChannelService delegateChannelService) {
        ServiceManager.delegateChannelService = delegateChannelService;
    }

    @Autowired
    public void setDelegateNodeService(DelegateNodeService delegateNodeService) {
        ServiceManager.delegateNodeService = delegateNodeService;
    }

    @Autowired
    public void setTopicService(TopicService topicService) {
        ServiceManager.topicService = topicService;
    }

    @Autowired
    public void setMessageCacheService(MessageCacheService messageCacheService) {
        ServiceManager.messageCacheService = messageCacheService;
    }

    @Autowired
    public void setBlackListService(BlackListService blackListService) {
        ServiceManager.blackListService = blackListService;
    }
}
