package com.huo.lconn.task;

import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import io.netty.channel.Channel;

public class TcpQueryTask extends AbstractQueryTask {

    public TcpQueryTask(Message.Query query, Channel tcpChannel) {
        super(query, tcpChannel);
    }

    @Override
    public void handleResponse(Message.RetCode retCode, Message.Query query, Channel channel) {
        Message.Response response = MessageCreator.buildResponse(retCode, query.getRequestId());
        channel.writeAndFlush(response);
    }
}