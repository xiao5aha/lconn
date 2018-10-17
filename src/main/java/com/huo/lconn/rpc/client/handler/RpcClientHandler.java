package com.huo.lconn.rpc.client.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import com.huo.lconn.rpc.client.RpcSingleClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcClientHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {

    private RpcSingleClient rpcClient;

    public RpcClientHandler(RpcSingleClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (log.isDebugEnabled()) {
            log.debug("rpc client {} channel {} read msg {}", rpcClient, ctx.channel(), TextFormat.shortDebugString(msg));
        }
        rpcClient.recieve(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (log.isDebugEnabled()) {
            log.debug("rpc client {} channel {} catch exception", rpcClient, ctx.channel());
        }
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                rpcClient.ping(ctx.channel());
            } else if (event.state().equals(IdleState.READER_IDLE)) {
                log.info("rpc client {} channel {} read idle", rpcClient, ctx.channel());
                ctx.channel().close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("rpc client {} channel {} inactive", rpcClient, ctx.channel());
        }
        rpcClient.unregister(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("rpc client {} channel {} active", rpcClient, ctx.channel());
        }
    }

}
