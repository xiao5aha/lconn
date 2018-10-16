package com.huo.lconn.codec;

import com.google.protobuf.GeneratedMessageV3;
import com.huo.lconn.message.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageMapper {
    public static final Map<Short, String> methodIdToRequestName = new HashMap<>();
    public static final Map<String, Class<? extends GeneratedMessageV3>> typeToMessageClass = new HashMap<>();
    public static final Map<Class<? extends GeneratedMessageV3>, String> classToMessageType = new HashMap<>();
    public static final Map<String, Short> requestNameToMethodId = new HashMap<>();

    static {
        // id 2 class
        methodIdToRequestName.put(MessageType.HEART_BEAT, Message.HeartBeat.class.getName());
        methodIdToRequestName.put(MessageType.REG_DEVICE, Message.RegDev.class.getName());
        methodIdToRequestName.put(MessageType.SUB_CHANNEL, Message.Subscribe.class.getName());
        methodIdToRequestName.put(MessageType.RESPONSE, Message.Response.class.getName());
        methodIdToRequestName.put(MessageType.RET_MESSAGE, Message.RetMsg.class.getName());
        methodIdToRequestName.put(MessageType.AUTH_BACKEND, Message.AuthBackend.class.getName());
        methodIdToRequestName.put(MessageType.PUBLISH_MESSAGE, Message.Publish.class.getName());
        methodIdToRequestName.put(MessageType.QUERY_CACHE, Message.Query.class.getName());
        // class 2 id
        requestNameToMethodId.put(Message.HeartBeat.class.getName(), MessageType.HEART_BEAT);
        requestNameToMethodId.put(Message.RegDev.class.getName(), MessageType.REG_DEVICE);
        requestNameToMethodId.put(Message.Subscribe.class.getName(), MessageType.SUB_CHANNEL);
        requestNameToMethodId.put(Message.Response.class.getName(), MessageType.RESPONSE);
        requestNameToMethodId.put(Message.RetMsg.class.getName(), MessageType.RET_MESSAGE);
        requestNameToMethodId.put(Message.AuthBackend.class.getName(), MessageType.AUTH_BACKEND);
        requestNameToMethodId.put(Message.Publish.class.getName(), MessageType.PUBLISH_MESSAGE);
        requestNameToMethodId.put(Message.Query.class.getName(), MessageType.QUERY_CACHE);
        // type 2 class
        typeToMessageClass.put("reg", Message.RegDev.class);
        typeToMessageClass.put("sub", Message.Subscribe.class);
        typeToMessageClass.put("resp", Message.Response.class);
        typeToMessageClass.put("ret", Message.RetMsg.class);
        // class 2 type
        for (Map.Entry<String, Class<? extends GeneratedMessageV3>> entry : typeToMessageClass.entrySet()) {
            classToMessageType.put(entry.getValue(), entry.getKey());
        }
    }
}