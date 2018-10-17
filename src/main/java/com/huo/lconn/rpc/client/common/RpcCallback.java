package com.huo.lconn.rpc.client.common;

import com.huo.lconn.message.Message;

public interface RpcCallback {

    void success(Message.RetCode ret);

    void fail(Throwable cause);

}