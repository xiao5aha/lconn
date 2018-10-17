package com.huo.lconn.backend;

public interface MessageProvider {

    void startProvide() ;

    void shutdown() ;

    int getPendingTasks();
}