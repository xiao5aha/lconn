package com.huo.lconn.backend.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.huo.lconn.cache.localcache.LocalCache;
import com.huo.lconn.message.Message;
import com.huo.lconn.task.TcpPubTask;
import com.huo.lconn.task.TcpQueryTask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BackendBusinessHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) throws Exception {
        if (msg instanceof Message.Publish) {
            Message.Publish pubMessage = (Message.Publish) msg;
            LocalCache.THREAD_POOL.execute(new TcpPubTask(pubMessage, ctx.channel()));
        } else if (msg instanceof Message.Query) {
            Message.Query queryMessage = (Message.Query) msg;
            LocalCache.THREAD_POOL.execute(new TcpQueryTask(queryMessage, ctx.channel()));
        } else {
            log.warn("server channel {} read unknow msg {}", ctx.channel(), TextFormat.shortDebugString(msg));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("server channel {} caught {}", ctx.channel(), cause);
        ctx.close();
    }
}
