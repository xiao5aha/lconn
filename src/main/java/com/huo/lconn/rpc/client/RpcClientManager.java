package com.huo.lconn.rpc.client;

public class RpcClientManager {

    private static volatile RpcMultiClient client;

    public static void initAndStart(RpcMultiClient client) {
        RpcClientManager.client = client;
        RpcClientManager.client.start();
    }

    public static RpcMultiClient getInstance() {
        return client;
    }

}