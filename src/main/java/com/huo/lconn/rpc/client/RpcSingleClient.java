package com.huo.lconn.rpc.client;

import com.google.protobuf.GeneratedMessageV3;
import com.huo.lconn.cache.localcache.LocalCache;
import com.huo.lconn.codec.Int32FrameDecoder;
import com.huo.lconn.codec.Int32FrameEncoder;
import com.huo.lconn.codec.ProtoBufDecoder;
import com.huo.lconn.codec.ProtoBufEncoder;
import com.huo.lconn.message.Message;
import com.huo.lconn.rpc.client.common.RpcCallback;
import com.huo.lconn.rpc.client.handler.RpcClientHandler;
import com.huo.lconn.utils.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.log4j.Log4j2;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class RpcSingleClient {

    private Bootstrap bootstrap;

    private SocketAddress remoteAddress;

    private volatile Channel currentChannel;
    /**
     * heartInterval in seconds
     */
    private final int heartInterval = 10;
    /**
     * connectTimeout in millseconds
     */
    private volatile int connectTimeout = 1000;

    private Map<Channel, ChannelExtra> channelExtraMap = new ConcurrentHashMap<>();

    /**
     * 每个长连接都对应一个ChannelExtra长连接扩展，该扩展中包含每次请求的requestId和callback（成功或者异常）的键值对
     * 意在说明长连接的某次调用成功了或者失败了！
     */
    private class ChannelExtra {
        private volatile Map<Integer, RpcCallback> callbackMap = new ConcurrentHashMap<>();
        private AtomicInteger requestId = new AtomicInteger();
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * 记录了第几次调用并缓存callBack
         *
         * @param callback
         * @return
         */
        public int addCallback(RpcCallback callback) {
            lock.readLock().lock();
            try {
                if (callbackMap == null) return -1;
                int newRequestId = requestId.getAndIncrement();
                if (newRequestId == -1) newRequestId = requestId.getAndIncrement();
                if (callback != null) {
                    callbackMap.put(newRequestId, callback);
                }
                return newRequestId;
            } finally {
                lock.readLock().unlock();
            }
        }

        public RpcCallback popCallback(Integer requestId) {
            lock.readLock().lock();
            try {
                if (callbackMap == null) return null;
                return callbackMap.remove(requestId);
            } finally {
                lock.readLock().unlock();
            }
        }

        public Collection<RpcCallback> popAllCallback() {
            lock.writeLock().lock();
            try {
                if (callbackMap == null) return Collections.emptyList();
                Collection<RpcCallback> ans = callbackMap.values();
                callbackMap = null;
                return ans;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public RpcSingleClient(SocketAddress remoteAddress, EventLoopGroup workGroup) {
        this.remoteAddress = remoteAddress;
        this.bootstrap = new Bootstrap()
                .group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler("netty"))
                                .addLast(new IdleStateHandler(heartInterval * 2, heartInterval, 0))
                                .addLast(new Int32FrameDecoder())
                                .addLast(new ProtoBufDecoder())
                                .addLast(new Int32FrameEncoder())
                                .addLast(new ProtoBufEncoder())
                                .addLast(new RpcClientHandler(RpcSingleClient.this));
                    }
                });
    }

    /**
     * 如果到其他Peer的长连接已经建立那么就获取，没有就创建
     *
     * @return
     */
    private Channel createOrGetChannel() {
        Channel workChannel = currentChannel;
        if (workChannel == null) {
            synchronized (this) {
                if (currentChannel == null) {
                    ChannelFuture future = bootstrap.connect(remoteAddress);
                    boolean ret = future.awaitUninterruptibly(connectTimeout);
                    if (ret && future.isSuccess()) {
                        Channel newChannel = future.channel();
                        channelExtraMap.put(newChannel, new ChannelExtra());
                        currentChannel = newChannel;
                        log.info("rpc client {} create new channel {}", this, currentChannel);
                    } else {
                        throw new RuntimeException("connect timeout");
                    }
                }
                workChannel = currentChannel;
            }
        }
        return currentChannel;
    }

    public void start() {
        log.debug("rpc client {} start", this);
        createOrGetChannel();
    }

    /**
     * 每个长连接对应一个ChannelExtra内部类，内部类里面又包含了callBack回调
     * 该方法设置了回调并返回requestId信息
     *
     * @param payload
     * @param callback
     * @return
     */
    public boolean invoke(Message.Publish payload, RpcCallback callback) {
        Message.Publish.Builder builder = Message.Publish.newBuilder(payload);
        if (log.isDebugEnabled()) {
            log.debug("rpc client {} try to invoke {}", this, StringUtil.briefFormat(payload));
        }
        try {
            Channel workChannel = createOrGetChannel();
            ChannelExtra extra = channelExtraMap.get(workChannel);
            if (extra != null) {
                int requestId = extra.addCallback(callback);
                builder.setRequestId(requestId);
                Message.Publish realPayload = builder.build();
                if (log.isDebugEnabled()) {
                    log.debug("rpc client {} channel {} write msg {}", this, workChannel, StringUtil.briefFormat(realPayload));
                }
                workChannel.writeAndFlush(realPayload);
                return true;
            } else {
                log.error("rpc client {} channel {} extra null", this, workChannel);
            }
        } catch (Exception e) {
            log.error("rpc client {} invoke exception", this, e);
        }
        return false;
    }

    /**
     * 长连接闲置的时候调用该方法
     * 给长连接发送心跳，实际上是一个requestId信息
     *
     * @param channel
     * @return
     */
    public boolean ping(Channel channel) {
        if (channel != currentChannel) return false;
        ChannelExtra extra = channelExtraMap.get(channel);
        if (extra != null) {
            int requestId = extra.addCallback(null);
            channel.writeAndFlush(Message.HeartBeat.newBuilder().setRequestId(requestId).build());
            return true;
        }
        return false;
    }

    /**
     * 关闭长连接
     */
    public void close() {
        if (currentChannel != null) {
            synchronized (this) {
                if (currentChannel != null) {
                    log.info("rpc client {} try to close channel {}", this, currentChannel);
                    currentChannel.close();
                    currentChannel = null;
                }
            }
        }
    }

    /**
     * 长连接收到消息的时候调用
     * 根据通道对应的extra拿到本次请求的requestId对应的callback，然后调用callback
     *
     * @param channel
     * @param result
     */
    public void recieve(Channel channel, GeneratedMessageV3 result) {
        if (result instanceof Message.Response) {
            Message.Response resp = (Message.Response) result;
            ChannelExtra extra = channelExtraMap.get(channel);
            if (extra != null) {
                final RpcCallback callback = extra.popCallback(resp.getRequestId());
                if (callback != null) {
                    asyncCallFinsh(callback, resp.getRetCode());
                }
            }
        }
    }

    /**
     * 注销长连接，清除掉长连接对应的Extra和extra内部callback
     *
     * @param channel
     */
    public void unregister(Channel channel) {
        if (currentChannel == channel) {
            synchronized (this) {
                if (currentChannel == channel) {
                    currentChannel = null;
                }
            }
        }
        ChannelExtra extra = channelExtraMap.remove(channel);
        if (extra != null) {
            for (RpcCallback callback : extra.popAllCallback()) {
                asyncCallFail(callback, new Exception("Channel closed"));
            }
        }
    }

    /**
     * 异步调用处理callback业务
     *
     * @param callback
     * @param result
     */
    private void asyncCallFinsh(final RpcCallback callback, final Message.RetCode result) {
        LocalCache.THREAD_POOL.execute(() -> callback.success(result));
    }

    /**
     * 异步调用处理异常
     *
     * @param callback
     * @param cause
     */
    private void asyncCallFail(final RpcCallback callback, final Throwable cause) {
        LocalCache.THREAD_POOL.execute(() -> callback.fail(cause));
    }

}