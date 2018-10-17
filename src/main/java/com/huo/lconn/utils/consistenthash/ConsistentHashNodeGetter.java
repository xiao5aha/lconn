package com.huo.lconn.utils.consistenthash;

import com.huo.lconn.constant.Constant;
import com.huo.lconn.utils.ServiceManager;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.util.StringUtils;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class ConsistentHashNodeGetter implements NodeGetter, Watcher {

    private volatile ConsistentHash<String> consistentHash;
    private Map<String, String> host2NodeMap = new ConcurrentHashMap<>();
    //公平锁,避免后发生的状态变化先获得锁,将节点设置错
    private Lock lock = new ReentrantLock(true);

    private ConsistentHashNodeGetter() {
        ServiceManager.curatorFramework.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.RECONNECTED || newState == ConnectionState.CONNECTED) {
                buildConsistentHash();
            }
        });
        buildConsistentHash();
    }

    @Override
    public String getNode(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            throw new NullPointerException();
        }
        String host = consistentHash.get(deviceId);
        return host2NodeMap.get(host);

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (log.isDebugEnabled()) {
            log.debug("zookeeper event triggered:" + watchedEvent);
        }
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            buildConsistentHash();
        }
    }

    private void buildConsistentHash() {
        log.info("build consistent hash start");
        try {
            final String rootNode = Constant.ZK_ROOT;
            final String nodeParent = Constant.ZK_NODE_PARENT;
            final String zkParent = rootNode + nodeParent;
            GetChildrenBuilder builder = ServiceManager.curatorFramework.getChildren();
            builder.usingWatcher(this);
            List<String> nodeLists = builder.forPath(zkParent);
            lock.lock();
            int replicas = Constant.NUM_OF_REPLICAS;
            ConsistentHash<String> tmpConsistentHash = new ConsistentHash<>(replicas, getHostList(nodeLists, zkParent));
            consistentHash = tmpConsistentHash;
            lock.unlock();
        } catch (Exception e) {
            log.error("handle zk error" + e.getMessage(), e);
        }
        log.info("build consistent hash end");
    }

    private List<String> getHostList(List<String> nodeList, String zkParent) {
        if (nodeList == null || nodeList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> hostList = new ArrayList<>();
        for (String node : nodeList) {
            try {
                byte[] data = ServiceManager.curatorFramework.getData().forPath(zkParent + "/" + node);
                String[] backendServers = new String(data, CharsetUtil.UTF_8).split("\\|");
                hostList.add(backendServers[0]);
                host2NodeMap.put(backendServers[0], backendServers[1]);
            } catch (Exception e) {
                log.error("read node data error,node is" + node, e);
            }
        }
        return hostList;
    }

    public static class InstanceHolder {
        public static ConsistentHashNodeGetter instance = new ConsistentHashNodeGetter();
    }

}