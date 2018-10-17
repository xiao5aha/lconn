package com.huo.lconn.utils.consistenthash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ConsistentHash<T> {
    /**
     * 虚节点数量
     */
    private final int numberOfReplicas;
    /**
     * 虚节点和节点映射
     */
    private volatile TreeMap<Integer, List<T>> circle = new TreeMap<>();
    /**
     * 圈大小
     */
    private static final int circleSize = 188833;
    //	private static final int circleSize = 18;
    /**
     *
     * @param numberOfReplicas	虚节点数
     * @param nodes	节点数
     */
    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            addNode(circle, node);
        }
    }

    public synchronized void add(T node) {
        TreeMap<Integer, List<T>> newCircle = copyCircle();
        addNode(newCircle, node);
        this.circle = newCircle;
    }

    /**
     * 删除节点
     * @param node
     */
    public synchronized void remove(T node)	{
        TreeMap<Integer, List<T>> newCircle = copyCircle();
        remove(newCircle, node);
        this.circle = newCircle;
    }

    private TreeMap<Integer, List<T>> copyCircle() {
        TreeMap<Integer, List<T>> newTree = new TreeMap<>();

        for (Map.Entry<Integer, List<T>> entry : circle.entrySet())	{
            List<T> list = new ArrayList<T>();
            list.addAll(entry.getValue());
            newTree.put(entry.getKey(), list);
        }
        return newTree;
    }

    /**
     * 添加节点
     * @param node
     */
    private void addNode(TreeMap<Integer, List<T>> circle, T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            // 节点与虚拟节点映射关系
            int key = hashMd5(node.toString() + i);
            List<T> list = circle.get(key);
            if (list == null) {
                list = new ArrayList<T>();
                circle.put(key, list);
            }
            if (!containsNode(list, node)) {
                list.add(node);
            }
        }
    }

    private void removeNodeToList(List<T> list, T node)	{
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (node.equals(it.next())) {
                it.remove();
            }
        }
    }

    private boolean containsNode(List<T> list, T node) {
        for (T t : list) {
            if (t.equals(node))	{
                return true;
            }
        }
        return false;
    }

    /**
     * 删除节点
     * @param node
     */
    private void remove(TreeMap<Integer, List<T>> circle, T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            // 节点与虚拟节点映射关系
            int key = hashMd5(node.toString() + i);
            List<T> list = circle.get(key);
            if (list != null) {
                if (list.contains(node)) {
                    removeNodeToList(list, node);
                }
                if (list.isEmpty())	{
                    circle.remove(key);
                }
            }
        }
    }

    /**
     * 得到key对应节点
     * @param key
     * @return
     */
    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        // 对key的特征进行映射，hash值为虚节点
        int hash = hashMd5(key);
        // 顺时针找到对应节点
        Map.Entry<Integer, List<T>> entry = circle.ceilingEntry(hash);
        List<T> node = null;
        if (entry == null) {//为空表示最大节点已经在键的左边了，需要把键映射到第一个节点
            node = circle.firstEntry().getValue();
        }
        else {
            node = entry.getValue();
        }

        if (node != null && !node.isEmpty()) {
            return node.get(0);
        }
        return null;
    }

    private static int hashCode(byte[] bytes) {
        int hash = 0;
        for (byte b : bytes) {
            hash = hash * 31 + ((int) b & 0xFF);
            if (hash > 0x4000000) {
                hash = hash % 0x4000000;
            }
        }
        return hash;
    }

    //	private static String bytesToString(byte[] data)
    //	{
    //		char hexDigits[] =
    //		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    //		char[] temp = new char[data.length * 2];
    //		for (int i = 0; i < data.length; i++)
    //		{
    //			byte b = data[i];
    //			temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
    //			temp[i * 2 + 1] = hexDigits[b & 0x0f];
    //		}
    //		return new String(temp);
    //
    //	}

    /**
     * md5映射计算
     * @param o
     * @return
     */
    private  int hashMd5(Object o) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(o.toString().getBytes());
            int hashCode = hashCode(bytes);
            return hashCode % circleSize;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }

}