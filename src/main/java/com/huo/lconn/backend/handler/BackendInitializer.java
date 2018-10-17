package com.huo.lconn.backend.handler;

import com.huo.lconn.client.handler.ClientHeartBeatHandler;
import com.huo.lconn.codec.Int32FrameDecoder;
import com.huo.lconn.codec.Int32FrameEncoder;
import com.huo.lconn.codec.ProtoBufDecoder;
import com.huo.lconn.codec.ProtoBufEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
        		.addLast(new LoggingHandler("netty"))
                .addLast(new ReadTimeoutHandler(25))
                .addLast(new Int32FrameDecoder())
                .addLast(new ProtoBufDecoder())
                .addLast(new Int32FrameEncoder())
                .addLast(new ProtoBufEncoder())
                .addLast(new ClientHeartBeatHandler())
                .addLast(new BackendBusinessHandler());
    }
}