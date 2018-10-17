package com.huo.lconn.task;

import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TcpPubTask extends AbstractPubTask {

    private final long taskStartTime = System.currentTimeMillis();

    public TcpPubTask(Message.Publish publish, Channel tcpChannel) {
        super(publish, tcpChannel);
    }

    @Override
    public void handleResponse(Message.RetCode retCode, Message.Publish publish, Channel channel) {
        Message.Response response = MessageCreator.buildResponse(retCode, publish.getRequestId());
        channel.writeAndFlush(response);
    }

}