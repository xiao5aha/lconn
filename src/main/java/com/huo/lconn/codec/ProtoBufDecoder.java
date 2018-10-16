package com.huo.lconn.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ProtoBufDecoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() > 2) {
            short methodId = in.readShort();
            byte[] message = new byte[in.readableBytes()];
            in.readBytes(message);
            out.add(decode(methodId, message));
        }
    }


    private static ConcurrentHashMap<String, Class<? extends GeneratedMessageV3>> name2classMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Method> name2methodMap = new ConcurrentHashMap<>();

    public static GeneratedMessageV3 decode(short methodId ,byte[] message) {

        String classNameStr = MessageMapper.methodIdToRequestName.get(methodId);
        Class<? extends GeneratedMessageV3> generatedMessageClass = name2classMap.get(classNameStr);
        Method parseMethod = name2methodMap.get(classNameStr);
        try {

            if (generatedMessageClass == null) {
                generatedMessageClass = (Class<? extends GeneratedMessageV3>) Class.forName(classNameStr);
                parseMethod = generatedMessageClass.getDeclaredMethod("parseFrom", byte[].class);
                name2classMap.put(classNameStr, generatedMessageClass);
                name2methodMap.put(classNameStr, parseMethod);
            }

            GeneratedMessageV3 generatedMessage = (GeneratedMessageV3) parseMethod.invoke(generatedMessageClass, message);
            return generatedMessage;
        } catch (Exception e) {
            log.warn("deocde message error.", e);
            return null;
        }
    }
}