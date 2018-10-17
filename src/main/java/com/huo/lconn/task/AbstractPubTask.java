package com.huo.lconn.task;

import com.huo.lconn.constant.Constant;
import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import com.huo.lconn.rpc.client.RpcClientManager;
import com.huo.lconn.rpc.client.common.RpcFuture;
import com.huo.lconn.utils.ServiceManager;
import com.huo.lconn.utils.StringUtil;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
public abstract class AbstractPubTask implements Runnable {
    private final Message.Publish publish;
    private final Channel channel;
    private final long taskStartTime = System.currentTimeMillis();

    public AbstractPubTask(Message.Publish publish, Channel channel) {
        this.publish = publish;
        this.channel = channel;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("server channel {} publish {}", channel, StringUtil.briefFormat(publish));
        }
        Message.PushType pushType = publish.getPushType();
        String realTopic = ServiceManager.topicService.getRealTopic(publish);

        Message.RetCode retCode = Message.RetCode.FAIL;
        if (pushType == Message.PushType.GROUP) {
            // 公告推送保持原有逻辑
            Message.RetMsg retMsg = MessageCreator.buildRetMsg(publish);
            ServiceManager.delegateChannelService.writeMessageAndFlush(realTopic, retMsg);
            retCode = Message.RetCode.SUCCESS;
        } else {
            Set<String> nodeSet = Collections.emptySet();
            if (pushType == Message.PushType.SPECIAL) {
                nodeSet = ServiceManager.delegateNodeService.getSingle(realTopic);
            } else if (pushType == Message.PushType.MULTI) {
                nodeSet = ServiceManager.delegateNodeService.getMulti(realTopic);
            }
            boolean success = false;
            // 1. 本机推送
            boolean localSuccess = pushLocal(nodeSet, pushType, realTopic, publish);
            if (localSuccess) {
                success = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("server channel {} push local result {}", channel, localSuccess);
            }
            // 2. 跨机器推送
            boolean remoteSuccess = pushRemote(nodeSet, publish);
            if (remoteSuccess) {
                success = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("server channel {} push remote result {}", channel, remoteSuccess);
            }
            // 3. 处理缓存
            if (!success) {
                if (publish.getExpiry() > System.currentTimeMillis()) {
                    ServiceManager.messageCacheService.addMessage(realTopic, publish.getBody().toByteArray(), publish.getExpiry());
                    retCode = Message.RetCode.CACHED;
                } else {
                    retCode = Message.RetCode.FAIL;
                }
            } else {
                retCode = Message.RetCode.SUCCESS;
            }
        }
        long taskFinishTime = System.currentTimeMillis();
        handleResponse(retCode, publish, channel);
        if (log.isDebugEnabled()) {
            log.debug("server channel {} final result {} cost {} ms", channel, retCode, taskFinishTime - taskStartTime);
        }
    }

    private boolean pushLocal(Set<String> nodeSet, Message.PushType pushType, String realTopic, Message.Publish publish) {
        if (nodeSet == null || nodeSet.size() == 0) {
            return pushLocal0(pushType, realTopic, publish);
        } else {
            for (String node : nodeSet) {
                if (ServiceManager.delegateNodeService.isMySelf(node)) {
                    return pushLocal0(pushType, realTopic, publish);
                }
            }
            return false;
        }
    }

    private boolean pushLocal0(Message.PushType pushType, String realTopic, Message.Publish publish) {
        Message.RetMsg retMsg = MessageCreator.buildRetMsg(publish);
        if (log.isDebugEnabled()) {
            log.debug("server channel {} push local", channel);
        }
        return ServiceManager.delegateChannelService.writeMessageAndFlush(realTopic, retMsg) > 0;
    }

    private boolean pushRemote(Set<String> nodeSet, Message.Publish publish) {
        if (nodeSet == null)
            return false;
        // 2.1 push
        List<RpcFuture> futureList = new ArrayList<RpcFuture>();
        for (String node : nodeSet) {
            if (!ServiceManager.delegateNodeService.isMySelf(node)) {
                RpcFuture future = new RpcFuture();
                if (log.isDebugEnabled()) {
                    log.debug("server channel {} rpc remote {}", channel, node);
                }
                RpcClientManager.getInstance().invoke(node, publish, future);
                futureList.add(future);
            }
        }
        // 2.2 收集结果
        long start = System.currentTimeMillis();
        long timeout = 1000L;
        for (RpcFuture future : futureList) {
            try {
                long realTimeout = timeout - (System.currentTimeMillis() - start);
                if (realTimeout > 0) {
                    Message.RetCode resp = future.get(realTimeout, TimeUnit.MILLISECONDS);
                    if (resp == Message.RetCode.SUCCESS) {
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                log.error("server channel {} rpc interrupted", channel);
                return false;
            } catch (ExecutionException e) {
                log.error("server channel {} rpc execution error", channel, e);
                // 执行异常查看其它机器结果
            } catch (TimeoutException e) {
                log.error("server channel {} rpc timeout", channel);
                return false;
            }
        }
        return false;
    }

    public abstract void handleResponse(Message.RetCode retCode, Message.Publish publish, Channel channel);

}