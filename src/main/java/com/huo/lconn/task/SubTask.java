package com.huo.lconn.task;

import com.google.protobuf.TextFormat;
import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import com.huo.lconn.utils.ServiceManager;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 15:29
 */
@Log4j2
@AllArgsConstructor
public class SubTask implements Runnable {

    private final DelegateChannel channel;
    private final Message.Subscribe subscribe;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("client channel {} sub command {}", channel, TextFormat.shortDebugString(subscribe));
        }
        Integer productCode = channel.getProductCode();
        if (productCode == null) {
            log.warn("client channel {} close due to unreg", channel);
            channel.close();
            return;
        }
        Message.SubType subType = subscribe.getSubType();
        String topic = subscribe.getTopic();
        Message.PushType pushType = subscribe.getPushType();
        if (StringUtils.isEmpty(topic)) {
            log.warn("client channel {} sub command topic is blank", channel);
            return;
        }
        String realTopic = ServiceManager.topicService.getRealTopic(subscribe, channel);
        if (subType == Message.SubType.SUB) {
            if (channel.getTopics().contains(realTopic)) {
                // ignore dup cmd
                if (log.isDebugEnabled()) {
                    log.debug("client channel {} sub command ignore", channel, TextFormat.shortDebugString(subscribe));
                }
                writeSuccessResponse();
            } else {
                // real sub
                // 1. sub in local
                ServiceManager.delegateChannelService.channelSubTopic(realTopic, channel);

                // 2. sub in global
                if (pushType != Message.PushType.GROUP) {
                    if (pushType == Message.PushType.SPECIAL) {
                        ServiceManager.delegateNodeService.addSingle(channel, realTopic);
                    } else if (pushType == Message.PushType.MULTI) {
                        ServiceManager.delegateNodeService.addMulti(channel, realTopic);
                    }
                }

                // 3. response
                writeSuccessResponse();

                // 4. pull cache msg
                if (pushType != Message.PushType.GROUP) {
                    Set<byte[]> cacheSet = ServiceManager.messageCacheService.getMessageBytes(realTopic);
                    if (cacheSet != null && cacheSet.size() != 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("client channel {} write {} message {}", channel, realTopic, cacheSet.size());
                        }
                        for (byte[] cache : cacheSet) {
                            Message.RetMsg retMsg = MessageCreator.buildRetMsg(subscribe, cache);
                            channel.writeAndFlush(retMsg);
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("client channel {} sub command finish", channel, TextFormat.shortDebugString(subscribe));
                }
            }
        } else {
            if (!channel.getTopics().contains(realTopic)) {
                // ignore dup cmd
                if (log.isDebugEnabled()) {
                    log.debug("client channel {} unsub command ignore", channel, TextFormat.shortDebugString(subscribe));
                }
                writeSuccessResponse();
            } else {
                // 1. unsub in local
                ServiceManager.delegateChannelService.channelUnSubTopic(realTopic, channel);

                // 2. unsub global
                if (pushType != Message.PushType.GROUP) {
                    if (pushType == Message.PushType.SPECIAL) {
                        ServiceManager.delegateNodeService.delSingle(channel, realTopic);
                    } else if (pushType == Message.PushType.MULTI) {
                        ServiceManager.delegateNodeService.delMulti(channel, realTopic);
                    }
                }

                // 3. response
                writeSuccessResponse();

                if (log.isDebugEnabled()) {
                    log.debug("client channel {} unsub command finish", channel, TextFormat.shortDebugString(subscribe));
                }
            }
        }
    }

    private void writeSuccessResponse() {
        Message.Response response = MessageCreator.buildResponse(Message.RetCode.SUCCESS, subscribe.getRequestId());
        channel.writeAndFlush(response);
    }
}
