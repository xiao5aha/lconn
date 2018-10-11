package com.huo.lconn.channel.entity;

import com.google.protobuf.GeneratedMessageV3;

import java.util.Set;

/**
 * @Author 小混蛋
 * @Desc 长连接
 */
public interface DelegateChannel {

    /**
     * 长连接可以对应多个主题，主题也可以对应多个长连接
     * @return
     */
    Set<String> getTopics();

    void writeAndFlush(GeneratedMessageV3 msg);

    void setDeviceId(String deviceId);

    void setProductCode(Integer productCode);

    Integer getProductCode();

    void close();

    String getDeviceId();

    long getReSubTime();

    void setReSubTime(long reSubTime);

    boolean isClosed();

    void setClosed(boolean closed);

    Set<String> getSingleTopics();

    Set<String> getMultiTopics();
}