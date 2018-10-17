package com.huo.lconn.rpc.server;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.huo.lconn.cache.localcache.LocalCache;
import com.huo.lconn.client.handler.ClientHeartBeatHandler;
import com.huo.lconn.codec.Int32FrameDecoder;
import com.huo.lconn.codec.Int32FrameEncoder;
import com.huo.lconn.codec.ProtoBufDecoder;
import com.huo.lconn.codec.ProtoBufEncoder;
import com.huo.lconn.constant.Constant;
import com.huo.lconn.message.Message;
import com.huo.lconn.node.NodeRegister;
import com.huo.lconn.task.RpcPublishTask;
import com.huo.lconn.utils.ServiceManager;
import com.huo.lconn.utils.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcServer {

    private NodeRegister node;

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private int heartTimeout;

    public RpcServer(EventLoopGroup workGroup) {
        String zkPath = Constant.ZK_PATH;
        this.port = Constant.DEFAULT_RPC_PORT;
        this.heartTimeout = Constant.HEART_TIME_OUT;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workGroup = workGroup;
        this.node = new NodeRegister(zkPath, "rpc", StringUtil.getByte(ServiceManager.delegateNodeService.getMySelf()));
    }

    public void start() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler("netty"))
                                .addLast(new ReadTimeoutHandler(heartTimeout))
                                .addLast(new Int32FrameDecoder())
                                .addLast(new ProtoBufDecoder())
                                .addLast(new Int32FrameEncoder())
                                .addLast(new ProtoBufEncoder())
                                .addLast(new ClientHeartBeatHandler())
                                .addLast(new RpcServerHandler(RpcServer.this));
                    }
                });
        try {
            ChannelFuture future = b.bind(port).sync();
            log.info("alive rpc server start port {}", port);
            node.start();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            shutdown();
        }
    }

    public void shutdown() {
        node.close();
    }

    /**
     * 该方法就是其他机器节点转发消息的时候调用的
     *
     * @param channel
     * @param msg
     */
    public void call(Channel channel, GeneratedMessageV3 msg) {
        if (msg instanceof Message.Publish) {
            Message.Publish pubMessage = (Message.Publish) msg;
            LocalCache.THREAD_POOL.execute(new RpcPublishTask(pubMessage, channel));
        } else {
            log.error("rpc server {} channel {} call unknow msg {}", this, channel, TextFormat.shortDebugString(msg));
        }
    }

}
