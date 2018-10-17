package com.huo.lconn.rpc.client.common;

import com.huo.lconn.message.Message;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RpcFuture implements Future<Message.RetCode>, RpcCallback {

    private static final int STATE_INIT = 0;
    private static final int STATE_SUCCESS = 0;
    private static final int STATE_FAIL = 0;

    private Object object = new Object();

    private volatile Message.RetCode result = null;

    private volatile Throwable failCause = null;

    private volatile int state = STATE_INIT;

    @Override
    public void success(Message.RetCode ret) {
        synchronized (object) {
            result = ret;
            state = STATE_SUCCESS;
            object.notifyAll();
        }
    }

    @Override
    public void fail(Throwable cause) {
        synchronized (object) {
            failCause = cause;
            state = STATE_FAIL;
            object.notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return state != STATE_INIT;
    }

    @Override
    public Message.RetCode get() throws InterruptedException, ExecutionException {
        synchronized (object) {
            if (state == STATE_INIT) {
                object.wait();
            }
        }
        if (state == STATE_SUCCESS) {
            return result;
        } else {
            throw new ExecutionException(failCause);
        }
    }

    @Override
    public Message.RetCode get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        synchronized (object) {
            if (state == STATE_INIT) {
                object.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
        if (state == STATE_SUCCESS) {
            return result;
        } else {
            throw new ExecutionException(failCause);
        }
    }

}
