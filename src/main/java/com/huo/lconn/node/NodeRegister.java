package com.huo.lconn.node;

import com.huo.lconn.utils.ServiceManager;
import lombok.extern.log4j.Log4j2;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;

@Log4j2
public class NodeRegister {

    private String path;

    private String prefix;

    private byte[] data;

    private volatile PersistentNode node;

    public NodeRegister(String path, String prefix, byte[] data) {
        this.prefix = prefix;
        this.path = path;
        this.data = data;
    }

    public void start() {
        if (log.isDebugEnabled()) {
            log.debug("node register start path {} prefix {}", path, prefix);
        }
        node = new PersistentNode(ServiceManager.curatorFramework, CreateMode.EPHEMERAL_SEQUENTIAL, true, path + "/" + prefix, data);
        node.start();
    }

    public void close() {
        if (log.isDebugEnabled()) {
            log.debug("node register close path {} prefix {}", path, prefix);
        }
        try {
            node.close();
        } catch (IOException e) {
            log.debug("node register path {} close catch exception", path, e);
        }
    }


}