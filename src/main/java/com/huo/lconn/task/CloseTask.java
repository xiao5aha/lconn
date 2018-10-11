package com.huo.lconn.task;

import com.huo.lconn.channel.entity.DelegateChannel;
import com.huo.lconn.utils.ServiceManager;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/11 15:26
 */
@Log4j2
@AllArgsConstructor
public class CloseTask implements Runnable{

    private DelegateChannel channel;

    @Override
    public void run() {
        if(log.isDebugEnabled()){
            log.debug("client channel {} close command", channel);
        }
        channel.setClosed(true);
        ServiceManager.delegateChannelService.closeChannel(channel);
        ServiceManager.delegateNodeService.closeChannel(channel);
    }
}
