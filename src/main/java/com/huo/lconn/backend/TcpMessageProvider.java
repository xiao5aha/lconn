package com.huo.lconn.backend;

import com.huo.lconn.backend.handler.BackendInitializer;
import com.huo.lconn.constant.Constant;
import com.huo.lconn.utils.ServiceManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

@Log4j2
public class TcpMessageProvider implements MessageProvider {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    boolean isEvicted = false;
    //for host
    private static final String ZOO_KEEPER_STR_HOST = "/host";
    //for ip
    private static final String ZOO_KEEPER_STR_NODE = "/node";
    private final int serverPort;
    private final int clientPort;
    private PersistentNode ipZkNode;
    private PersistentNode hostZkNode;

    //初始化，ZK上创建两个永久顺序节点，一个是hostName+port，一个是ip+port，这两个节点目前还不知道是干嘛的
    public TcpMessageProvider(EventLoopGroup workerGroup) {
        serverPort = Constant.NETTY_BACKENT_PORT;
        clientPort = Constant.NETTY_CLIENT_PORT;
        bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = workerGroup;
        String rootNode = Constant.ZK_ROOT;
        String hostParent = Constant.ZK_HOST_PARENT;
        String ipParent = Constant.ZK_NODE_PARENT;
        String ip = ServiceManager.delegateNodeService.getMySelf();
        String hostNode = ip + ":" + serverPort;
        String ipNode = ip + ":" + clientPort;
        log.info("alive server register to zookeeper evictNodeIps=" + "ip=" + ip + "hostNode=" + hostNode + "ipNode=" + ipNode + "rootNode=" + rootNode + "hostParent=" + hostParent + "ipParent=" + ipParent);
        try {
            ipZkNode = new PersistentNode(ServiceManager.curatorFramework,
                    CreateMode.EPHEMERAL_SEQUENTIAL,
                    true,
                    rootNode + hostParent + ZOO_KEEPER_STR_HOST,
                    hostNode.getBytes("UTF8")
            );
            hostZkNode = new PersistentNode(ServiceManager.curatorFramework,
                    CreateMode.EPHEMERAL_SEQUENTIAL,
                    true,
                    rootNode + ipParent + ZOO_KEEPER_STR_NODE,
                    (hostNode + "|" + ipNode).getBytes("UTF8")
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("alive server register to zookeeper ipNode {} hostNode {}", ipNode, hostNode);
    }

    //这个相当于启动了一个netty服务端，并且真正的在zk上创建两个节点
    @Override
    public void startProvide() {
        try {
            System.setProperty("io.netty.allocator.type", "pooled");
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new BackendInitializer())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.AUTO_READ, true)
                    .childOption(ChannelOption.SO_LINGER, 0);
            ChannelFuture f = b.bind(serverPort).sync();
            log.info("alive server cp message provider service on port {}", serverPort);
            register2ZooKeeper();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    private void register2ZooKeeper() {
        if (!isEvicted) {
            ipZkNode.start();
            hostZkNode.start();
        }
    }

    @Override
    public void shutdown() {
        try {
            ipZkNode.close();
            hostZkNode.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public int getPendingTasks() {
        int count = 0;
        Iterator<EventExecutor> it = workerGroup.iterator();
        while (it.hasNext()) {
            SingleThreadEventExecutor ste = (SingleThreadEventExecutor) it.next();
            count += ste.pendingTasks();
        }
        return count;
    }
}