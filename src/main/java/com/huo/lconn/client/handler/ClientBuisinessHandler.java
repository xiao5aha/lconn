package com.huo.lconn.client.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.huo.lconn.cache.localcache.LocalCache;
import com.huo.lconn.channel.entity.TcpChannel;
import com.huo.lconn.task.CloseTask;
import com.huo.lconn.task.DispatchTask;
import com.huo.lconn.utils.ServiceManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/10 20:04
 */
@Log4j2
public class ClientBuisinessHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        TcpChannel channel = LocalCache.getTcpChannel(ctx.channel());
        LocalCache.THREAD_POOL.execute(new DispatchTask(channel, msg));
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        if (ServiceManager.blackListService.isInBlackList(remoteAddress.getHostString())) {
            log.warn("client channel {} ip in black", ctx.channel());
            ctx.channel().close();
        }
        LocalCache.getTcpChannel(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        TcpChannel channel = LocalCache.remove(ctx.channel());
        if (channel != null) {
            LocalCache.THREAD_POOL.execute(new CloseTask(channel));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("client channel {} exception", ctx.channel(), cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("client channel {} active", ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("client channel {} inactive", ctx.channel());
        }
    }

}
