package com.huo.lconn.client.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ClientHeartBeatHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) throws Exception {
        if (msg instanceof Message.HeartBeat) {
            Message.HeartBeat heartBeat = (Message.HeartBeat) msg;
            ctx.writeAndFlush(MessageCreator.buildHeartBeat(heartBeat.getRequestId()));
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}