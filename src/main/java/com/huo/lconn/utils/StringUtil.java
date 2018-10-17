package com.huo.lconn.utils;

import com.huo.lconn.message.Message;

import java.io.UnsupportedEncodingException;

public class StringUtil {

    public static String briefFormat(Message.Query query) {
        StringBuilder sb = new StringBuilder();
        sb.append("productCode ").append(query.getProductCode());
        sb.append(" pushType ").append(query.getPushType());
        sb.append(" topic ").append(query.getTopic());
        if (query.getPushType() == Message.PushType.SPECIAL) {
            sb.append(" deviceId ").append(query.getDeviceId());
        } else if (query.getPushType() == Message.PushType.MULTI) {
            sb.append(" deviceId ").append(query.getDeviceId());
            sb.append(" accountId ").append(query.getAccountId());
        }
        return sb.toString();
    }

    public static String briefFormat(Message.Publish publish) {
        StringBuilder sb = new StringBuilder();
        sb.append("productCode ").append(publish.getProductCode());
        sb.append(" pushType ").append(publish.getPushType());
        sb.append(" topic ").append(publish.getTopic());
        if (publish.getPushType() == Message.PushType.SPECIAL) {
            sb.append(" deviceId ").append(publish.getDeviceId());
        } else if (publish.getPushType() == Message.PushType.MULTI) {
            sb.append(" deviceId ").append(publish.getDeviceId());
            sb.append(" accountId ").append(publish.getAccountId());
        }
        sb.append(" expiry ").append(publish.getExpiry());
        return sb.toString();
    }

    public static String create(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not support");
        }
    }

    public static byte[] getByte(String data) {
        if (data == null) return null;
        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not support");
        }
    }

}