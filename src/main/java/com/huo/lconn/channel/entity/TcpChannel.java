package com.huo.lconn.channel.entity;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/10 20:17
 */
@AllArgsConstructor
public class TcpChannel extends AbstractChannel {

    private Channel channel;

    @Override
    public void writeAndFlush(GeneratedMessageV3 msg) {
        channel.writeAndFlush(msg);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public String toString() {
        return channel.toString();
    }

}
