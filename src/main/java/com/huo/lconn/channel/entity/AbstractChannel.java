package com.huo.lconn.channel.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/28 14:15
 */
public abstract class AbstractChannel implements DelegateChannel {
    String deviceId;
    Integer productCode;
    Set<String> topics = new HashSet<>();
    Set<String> singleTopics = new HashSet<>();
    Set<String> multiTopics = new HashSet<>();
    boolean isClosed = false;
    long reOrderTime;

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Integer getProductCode() {
        return productCode;
    }

    @Override
    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    @Override
    public Set<String> getTopics() {
        return topics;
    }

    @Override
    public long getReSubTime() {
        return reOrderTime;
    }

    @Override
    public void setReSubTime(long nextResubTime) {
        this.reOrderTime = nextResubTime;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    @Override
    public Set<String> getSingleTopics() {
        return singleTopics;
    }

    @Override
    public Set<String> getMultiTopics() {
        return multiTopics;
    }


}
