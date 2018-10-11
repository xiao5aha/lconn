package com.huo.lconn.topic;

import com.alibaba.druid.util.StringUtils;
import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.constant.Constant;
import com.huo.lconn.message.Message;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 15:58
 */
@Log4j2
@Service
public class TopicService {

    public String getRealTopic(String topic, Message.PushType pushType, String extra, Integer productCode) {
        if (productCode == null) {
            if (log.isDebugEnabled()) {
                log.debug("topic util getRealTopic productCode is null");
            }
            return null;
        }
        if (pushType == Message.PushType.SPECIAL && StringUtils.isEmpty(extra)) {
            if (log.isDebugEnabled()) {
                log.debug("topic util get real topic deviceId is null");
            }
            return null;
        }
        if (pushType == Message.PushType.GROUP) {
            return topic + ":" + productCode + ":" + pushType;
        } else {
            return topic + ":" + productCode + ":" + pushType + ":" + extra;
        }
    }

    public String getRealTopic(Message.Subscribe subscribe, DelegateChannel channel) {
        if (subscribe == null) return null;

        Integer productCode = channel.getProductCode();
        String extra = null;

        if (subscribe.getPushType() == Message.PushType.SPECIAL) {
            extra = channel.getDeviceId();
        } else if (subscribe.getPushType() == Message.PushType.MULTI) {
            extra = subscribe.getAccountId();
        }
        return getRealTopic(subscribe.getTopic(), subscribe.getPushType(), extra, productCode);
    }

    public String getRealTopic(Message.Subscribe subscribe, Channel channel) {
        if (subscribe == null) return null;
        Integer productCode = channel.attr(Constant.PRODUCT_CODE).get();
        String extra = null;
        if (subscribe.getPushType() == Message.PushType.SPECIAL) {
            extra = channel.attr(Constant.DEVICE_ID).get();
        } else if (subscribe.getPushType() == Message.PushType.MULTI) {
            extra = subscribe.getAccountId();
        }
        return getRealTopic(subscribe.getTopic(), subscribe.getPushType(), extra, productCode);
    }

    public String getRealTopic(Message.Query query) {
        if (query == null) return null;
        String extra = null;
        if (query.getPushType() == Message.PushType.SPECIAL) {
            extra = query.getDeviceId();
        } else if (query.getPushType() == Message.PushType.MULTI) {
            extra = query.getAccountId();
        }
        return getRealTopic(query.getTopic(), query.getPushType(), extra, query.getProductCode());
    }


    public String getRealTopic(Message.Publish publish) {
        if (publish == null) return null;
        String extra = null;
        if (publish.getPushType() == Message.PushType.SPECIAL) {
            extra = publish.getDeviceId();
        } else if (publish.getPushType() == Message.PushType.MULTI) {
            extra = publish.getAccountId();
        }
        return getRealTopic(publish.getTopic(), publish.getPushType(), extra, publish.getProductCode());
    }
}
