package com.huo.lconn.task;

public interface Auth<T> {

    boolean doAuth(T token);

    enum AuthType {
        CLIENT_HTTP, SERVER_MD5;
    }

}