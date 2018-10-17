package com.huo.lconn.client;

import com.huo.lconn.client.handler.ClientInitializer;
import com.huo.lconn.constant.Constant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import lombok.extern.log4j.Log4j2;

import java.util.Iterator;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/28 11:06
 */
@Log4j2
public class ClientServer {
    private final int port;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;
    private Channel pushserverChannel;

    public ClientServer(EventLoopGroup workerEventLoopGroup) {
        port = Constant.NETTY_CLIENT_PORT;
        bossEventLoopGroup = new NioEventLoopGroup(1);
        this.workerEventLoopGroup = workerEventLoopGroup;
    }

    public void start() {
        try {
            Channel channel = initNetty();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("start client error : " + e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    private Channel initNetty() throws InterruptedException {
        System.setProperty("io.netty.allocator.type", "pooled");
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossEventLoopGroup, workerEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ClientInitializer())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.SO_LINGER, 0);

        ChannelFuture f = b.bind(port).sync();
        log.info("alive client server on port {}", port);

        pushserverChannel = f.channel();
        return pushserverChannel;
    }

    public void shutdown() {
        log.info("alive client server shutdown...");
        bossEventLoopGroup.shutdownGracefully();
        workerEventLoopGroup.shutdownGracefully();

        log.info("alive client server closed port {}", port);
    }

    public int getPendingTasks() {
        int count = 0;
        Iterator<EventExecutor> it = workerEventLoopGroup.iterator();
        while (it.hasNext()) {
            SingleThreadEventExecutor ste = (SingleThreadEventExecutor) it.next();
            count += ste.pendingTasks();
        }
        return count;
    }
}
