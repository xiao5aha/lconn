package com.huo.lconn.task;

import com.huo.lconn.message.Message;
import com.huo.lconn.utils.ServiceManager;
import com.huo.lconn.utils.StringUtil;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractQueryTask implements Runnable {

    private final Message.Query query;
    private final Channel channel;

    public AbstractQueryTask(Message.Query query, Channel channel) {
        this.query = query;
        this.channel = channel;
    }

    @Override
    public void run() {
        long taskStartTime = System.currentTimeMillis();
        Message.RetCode retCode = QueryProcess();
        handleResponse(retCode, query, channel);
        long taskFinishTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("server channel {} final result {} cost {} ms", channel, retCode, taskFinishTime - taskStartTime);
        }
    }

    private Message.RetCode QueryProcess() {
        if (log.isDebugEnabled()) {
            log.debug("server channel {} query {}", channel, StringUtil.briefFormat(query));
        }
        Message.RetCode retCode = null;

        if (query.getPushType() == Message.PushType.GROUP) {
            retCode = Message.RetCode.PARAM_ERR;
        } else {
            String realTopic = ServiceManager.topicService.getRealTopic(query);
            if (ServiceManager.messageCacheService.checkMessageCached(realTopic)) {
                retCode = Message.RetCode.SUCCESS;
            } else {
                retCode = Message.RetCode.FAIL;
            }
        }
        return retCode;
    }

    public abstract void handleResponse(Message.RetCode retCode, Message.Query query, Channel channel);
}
