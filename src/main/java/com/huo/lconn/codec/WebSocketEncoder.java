package com.huo.lconn.codec;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.googlecode.protobuf.format.JsonJacksonFormat;
import com.huo.lconn.message.Message;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class WebSocketEncoder {

    public static void main(String[] args) {
        Message.RetMsg.Builder builder = Message.RetMsg.newBuilder();
        builder.setBody(ByteString.copyFrom(new byte[10]));
        builder.setTopic("test");
        builder.setPushType(Message.PushType.GROUP);
        Message.RetMsg ret = builder.build();
        System.out.println(toPacket(ret));
        System.out.println(toProtobuf(toPacket(ret)));
    }

    public static Packet toPacket(GeneratedMessageV3 msg) {
        Packet pkt = new Packet();
        pkt.setType(MessageMapper.classToMessageType.get(msg.getClass()));
        JsonJacksonFormat fmt = new JsonJacksonFormat();
        pkt.setData(fmt.printToString(msg));
        return pkt;
    }

    public static GeneratedMessageV3 toProtobuf(Packet pkt) {
        try {
            InputStream is = new ByteArrayInputStream(pkt.getData().getBytes(StandardCharsets.UTF_8));
            Class<? extends GeneratedMessageV3> clazz = MessageMapper.typeToMessageClass.get(pkt.getType());
            Method m = clazz.getDeclaredMethod("newBuilder");
            GeneratedMessage.Builder builder = (GeneratedMessage.Builder) m.invoke(clazz);
            JsonJacksonFormat fmt = new JsonJacksonFormat();
            fmt.merge(is, builder);
            is.close();
            return (GeneratedMessageV3) builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}