package com.huo.lconn.rpc.server;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcServerHandler extends SimpleChannelInboundHandler<GeneratedMessageV3> {
	
	private RpcServer server;
	
	public RpcServerHandler(RpcServer server) {
		this.server = server;
	}
	
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GeneratedMessageV3 msg) throws Exception {
		server.call(ctx.channel(), msg);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
    	if(log.isDebugEnabled()) {
    		log.debug("rpc server channel {} caught exception", ctx.channel(), cause);
    	}
        ctx.channel().close();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	if(log.isDebugEnabled()) {
    		log.debug("rpc server channel {} active", ctx.channel());
    	}
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	if(log.isDebugEnabled()) {
    		log.debug("rpc server channel {} inactive", ctx.channel());
    	}
    }

}