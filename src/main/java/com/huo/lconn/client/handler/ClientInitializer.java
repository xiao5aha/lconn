package com.huo.lconn.client.handler;

import com.huo.lconn.codec.Int32FrameDecoder;
import com.huo.lconn.codec.Int32FrameEncoder;
import com.huo.lconn.codec.ProtoBufDecoder;
import com.huo.lconn.codec.ProtoBufEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new LoggingHandler("nettyClient"))
                .addLast(new ReadTimeoutHandler(25))
                .addLast(new Int32FrameDecoder())
                .addLast(new ProtoBufDecoder())
                .addLast(new Int32FrameEncoder())
                .addLast(new ProtoBufEncoder())
                .addLast(new ClientHeartBeatHandler())
                .addLast(new ClientBuisinessHandler());
    }
}