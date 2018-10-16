package com.huo.lconn.codec;

import com.huo.lconn.message.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageType {
    public static final short HEART_BEAT = 0;
    public static final short REG_DEVICE = 1;
    public static final short SUB_CHANNEL = 2;
    public static final short RESPONSE = 257;
    public static final short RET_MESSAGE = 258;
    public static final short AUTH_BACKEND = 513;
    public static final short PUBLISH_MESSAGE = 514;
    public static final short QUERY_CACHE = 515;
    public static Map<Short, String> methodIdToRequestName = new HashMap();
    public static Map<String, Short> requestNameToMethodId = new HashMap();

    public MessageType() {
    }

    static {
        methodIdToRequestName.put(Short.valueOf((short) 0), Message.HeartBeat.class.getName());
        methodIdToRequestName.put(Short.valueOf((short) 1), Message.RegDev.class.getName());
        methodIdToRequestName.put(Short.valueOf((short) 2), Message.Subscribe.class.getName());
        methodIdToRequestName.put((short) 257, Message.Response.class.getName());
        methodIdToRequestName.put((short) 258, Message.RetMsg.class.getName());
        methodIdToRequestName.put((short) 513, Message.AuthBackend.class.getName());
        methodIdToRequestName.put((short) 514, Message.Publish.class.getName());
        methodIdToRequestName.put((short) 515, Message.Query.class.getName());
        requestNameToMethodId.put(Message.HeartBeat.class.getName(), Short.valueOf((short) 0));
        requestNameToMethodId.put(Message.RegDev.class.getName(), Short.valueOf((short) 1));
        requestNameToMethodId.put(Message.Subscribe.class.getName(), Short.valueOf((short) 2));
        requestNameToMethodId.put(Message.Response.class.getName(), (short) 257);
        requestNameToMethodId.put(Message.RetMsg.class.getName(), (short) 258);
        requestNameToMethodId.put(Message.AuthBackend.class.getName(), (short) 513);
        requestNameToMethodId.put(Message.Publish.class.getName(), (short) 514);
        requestNameToMethodId.put(Message.Query.class.getName(), (short) 515);
    }
}