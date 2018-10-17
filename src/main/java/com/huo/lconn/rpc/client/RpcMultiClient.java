package com.huo.lconn.rpc.client;

import com.google.protobuf.TextFormat;
import com.huo.lconn.constant.Constant;
import com.huo.lconn.message.Message;
import com.huo.lconn.node.Node;
import com.huo.lconn.node.NodeDiscover;
import com.huo.lconn.node.NodeListener;
import com.huo.lconn.rpc.client.common.RpcCallback;
import com.huo.lconn.utils.ServiceManager;
import io.netty.channel.EventLoopGroup;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

@Log4j2
public class RpcMultiClient {

    private NodeDiscover discover;

    private final int rpcPort;

    private EventLoopGroup workGroup;

    private volatile Map<String, RpcSingleClient> remoteMap = new HashMap<String, RpcSingleClient>();

    public RpcMultiClient(EventLoopGroup workGroup) {
        String zkPath = Constant.ZK_PATH;
        rpcPort = Constant.DEFAULT_RPC_PORT;
        this.workGroup = workGroup;
        discover = new NodeDiscover(zkPath);
        //这段代码就注册了个监听器，监听器触发的事件是从zk节点上获取到其他peer的IP，并做成一个IP RpcSingleClient的键值对
        discover.registerListener(new NodeListener() {
            @Override
            public void notifyChanged(List<Node> nodeList) {
                //remoteSet保存的是其他节点的IP信息
                Set<String> remoteSet = new HashSet<String>();
                for (Node node : nodeList) {
                    try {
                        String remote = new String(node.getNodeData(), "UTF-8");
                        remoteSet.add(remote);
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                remoteSet.remove(ServiceManager.delegateNodeService.getMySelf());

                Map<String, RpcSingleClient> newRemoteMap = new HashMap<String, RpcSingleClient>();
                for (String remote : remoteSet) {
                    if (remoteMap.containsKey(remote)) {
                        newRemoteMap.put(remote, remoteMap.get(remote));
                    } else {
                        newRemoteMap.put(remote, createRemoteRpcClient(remote));
                    }
                }

                Map<String, RpcSingleClient> oldRemoteMap = remoteMap;
                remoteMap = newRemoteMap;

                for (Map.Entry<String, RpcSingleClient> entry : oldRemoteMap.entrySet()) {
                    if (!remoteSet.contains(entry.getKey())) {
                        entry.getValue().close();
                    }
                }
            }
        });
    }

    public boolean invoke(String remote, Message.Publish command, RpcCallback callback) {
        RpcSingleClient client = getRemoteRpcClient(remote);
        if (client == null) {
            log.error("rpc client {} invoke remote {} command {}", this, remote, TextFormat.shortDebugString(command));
            return false;
        } else {
            return client.invoke(command, callback);
        }
    }

    private RpcSingleClient getRemoteRpcClient(String remote) {
        return remoteMap.get(remote);
    }

    private RpcSingleClient createRemoteRpcClient(String remote) {
        SocketAddress remoteAddress = new InetSocketAddress(remote, rpcPort);
        return new RpcSingleClient(remoteAddress, workGroup);
    }

    public void start() {
        discover.start();
    }

    public void shutdown() {
        discover.close();
    }

}