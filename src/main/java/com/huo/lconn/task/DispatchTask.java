package com.huo.lconn.task;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.message.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 14:24
 */
@Log4j2
@AllArgsConstructor
public class DispatchTask implements Runnable {

    private final DelegateChannel channel;
    private final GeneratedMessageV3 msg;

    @Override
    public void run() {
        if (msg instanceof Message.RegDev) {
            new RegTask(channel, (Message.RegDev) msg).run();
        } else if (msg instanceof Message.Subscribe) {
            new SubTask(channel, (Message.Subscribe) msg).run();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("client channel {} unknown command", channel, TextFormat.shortDebugString(msg));
            }
        }
    }
}
