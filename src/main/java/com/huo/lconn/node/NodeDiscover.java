package com.huo.lconn.node;

import com.alibaba.druid.util.StringUtils;
import com.huo.lconn.utils.ServiceManager;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public class NodeDiscover implements Watcher {

    private String path;

    private List<NodeListener> listenerList = new ArrayList<>();

    private volatile List<Node> currentNodeList = Collections.emptyList();

    //名字叫节点发现器
    public NodeDiscover(String path) {
        this.path = path;
    }

    //节点发现器做了什么呢？首先是注册了监听器
    public void start() {
        //当发现连接状态有改变的时候，比如新状态为建立连接或者重新建立连接，那么执行queryAndWatch方法，此连接应该是节点与zk的连接
        ServiceManager.curatorFramework.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED) {
                queryAndWatch();
            }
        });
        queryAndWatch();
    }

    public List<Node> getNodes() {
        return currentNodeList;
    }

    public void registerListener(NodeListener listener) {
        if (listener != null) {
            listenerList.add(listener);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("node discover zk trigger event {}", event);
        }
        //如果节点的子节点出现变化就调用更新方法
        if (event.getType() == Event.EventType.NodeChildrenChanged
                && StringUtils.equals(event.getPath(), path)) {
            queryAndWatch();
        }
    }

    //节点与zk建立连接之后执行此方法
    private void queryAndWatch() {
        try {
            //后台监控
            //异步执行完watcher的process方法后就执行这个方法
            ServiceManager.curatorFramework.getChildren().usingWatcher(NodeDiscover.this).inBackground((client, event) -> {
                List<String> nodeNameList = event.getChildren();
                List<Node> nodeList = new ArrayList<>();
                for (String nodeName : nodeNameList) {
                    try {
                        Node node = new Node();
                        byte[] nodeData = client.getData().forPath(path + "/" + nodeName);
                        node.setNodeName(nodeName);
                        node.setNodeData(nodeData);
                        nodeList.add(node);
                    } catch (Exception e) {
                        log.error("node discover get node {} catch exception", nodeName, e);
                    }
                }
                currentNodeList = nodeList;
                for (NodeListener listener : listenerList) {
                    listener.notifyChanged(nodeList);
                }
            }).forPath(path);
        } catch (Exception e) {
            log.error("node discover query and watch exception", e);
        }
    }

    public void close() {
    }
}
