package com.huo.lconn.cache.localcache;

import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.channel.entity.TcpChannel;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/28 15:21
 */
public class LocalCache {

    private static Map<Channel, TcpChannel> tcpChannelMap = new ConcurrentHashMap<>();
    /**
     * 缓存主题对应的长连接集合
     */
    public static ConcurrentHashMap<String, Set<DelegateChannel>> TOPIC_CHANNELS_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存长连接
     */
    public static Set<DelegateChannel> CHANNEL_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 获取客户端长连接
     *
     * @param channel
     * @return
     */
    public static TcpChannel getTcpChannel(Channel channel) {
        TcpChannel ret = tcpChannelMap.get(channel);
        if (ret == null) {
            ret = new TcpChannel(channel);
            tcpChannelMap.put(channel, ret);
        }
        return ret;
    }

    /**
     * 移除本地缓存中的客户端长连接
     *
     * @param channel
     * @return
     */
    public static TcpChannel remove(Channel channel) {
        return tcpChannelMap.remove(channel);
    }

    /**
     * 获取本地缓存长连接集合大小
     *
     * @return
     */
    public static int getSize() {
        return tcpChannelMap.size();
    }

    /**
     * 执行非netty任务的线程池
     */
    public static ExecutorService THREAD_POOL = new ThreadPoolExecutor(10, 20, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(20));

}
