package com.huo.lconn.task;

import com.google.protobuf.TextFormat;
import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import com.huo.lconn.utils.ServiceManager;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 14:31
 * @Desc: 长连接注册服务器节点类，完成了长连接设置，长连接注册和节点注册
 */
@Log4j2
@AllArgsConstructor
public class RegTask implements Runnable {

    private final DelegateChannel channel;
    private final Message.RegDev regDev;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("client channel {} reg command {}", channel, TextFormat.shortDebugString(regDev));
        }
        channel.setProductCode(regDev.getProductCode());
        channel.setDeviceId(regDev.getDeviceId());
        ServiceManager.delegateChannelService.regChannel(channel);
        ServiceManager.delegateNodeService.setNextResubTime(channel);
        Message.Response response = MessageCreator.buildResponse(Message.RetCode.SUCCESS, regDev.getRequestId());
        channel.writeAndFlush(response);
    }
}
