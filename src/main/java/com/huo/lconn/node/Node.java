package com.huo.lconn.node;

/**
 * zk中的节点，节点名称和节点内容
 */
public class Node {
    String nodeName;
    byte[] nodeData;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public byte[] getNodeData() {
        return nodeData;
    }

    public void setNodeData(byte[] nodeData) {
        this.nodeData = nodeData;
    }

}