package com.huo.lconn.task;

import com.huo.lconn.message.Message;
import com.huo.lconn.message.MessageCreator;
import com.huo.lconn.utils.ServiceManager;
import com.huo.lconn.utils.StringUtil;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RpcPublishTask implements Runnable {

    private Message.Publish publish;

    private Channel channel;

    private long taskStartTime = System.currentTimeMillis();

    public RpcPublishTask(Message.Publish publish, Channel channel) {
        this.channel = channel;
        this.publish = publish;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("rpc sever channel {} publish command {}", channel, StringUtil.briefFormat(publish));
        }
        String realTopic = ServiceManager.topicService.getRealTopic(publish);
        Message.RetMsg retMsg = MessageCreator.buildRetMsg(publish);
        //获取到主题之后，往主题里发送retMsg消息
        boolean success = ServiceManager.delegateChannelService.writeMessageAndFlush(realTopic, retMsg) > 0;
        Message.RetCode retCode;
        if (success) {
            retCode = Message.RetCode.SUCCESS;
        } else {
            retCode = Message.RetCode.FAIL;
        }
        Message.Response response = MessageCreator.buildResponse(retCode, publish.getRequestId());
        channel.writeAndFlush(response);
        long taskFinshTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("rpc server channel {} publish {} result {} cost {} ms", channel, StringUtil.briefFormat(publish), retCode, (taskFinshTime - taskStartTime));
        }
    }

}