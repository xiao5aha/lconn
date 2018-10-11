package com.huo.lconn.message;

import com.google.protobuf.ByteString;
import org.springframework.util.StringUtils;

public class MessageCreator {


    public static Message.Response buildResponse(Message.RetCode retCode, int requestId) {
        return buildResponse(retCode, requestId, null);
    }

    public static Message.Response buildResponse(Message.RetCode retCode, int requestId, String rightHost) {
        Message.Response.Builder builder = Message.Response.newBuilder();
        builder.setRetCode(retCode);
        builder.setRequestId(requestId);
        if (!StringUtils.isEmpty(rightHost)) {
            builder.setRightHost(rightHost);
        }
        return builder.build();

    }

    public static Message.RetMsg buildRetMsg(Message.Subscribe subscribe, byte[] body) {
        Message.RetMsg.Builder builder = Message.RetMsg.newBuilder();
        builder.setTopic(subscribe.getTopic());
        builder.setPushType(subscribe.getPushType());
        builder.setBody(ByteString.copyFrom(body));
        return builder.build();
    }


    public static Message.RetMsg buildRetMsg(Message.Publish publish) {
        Message.RetMsg.Builder builder = Message.RetMsg.newBuilder();
        builder.setBody(publish.getBody());
        builder.setTopic(publish.getTopic());
        builder.setPushType(publish.getPushType());
        return builder.build();
    }

    public static Message.HeartBeat buildHeartBeat(int requestId) {
        Message.HeartBeat.Builder builder = Message.HeartBeat.newBuilder();
        builder.setRequestId(requestId);
        return builder.build();
    }

    public static Message.Query buildQuery(int requestId, int productCode, String topic, int pushType, String deviceId, String accountId) {
        Message.Query.Builder builder = Message.Query.newBuilder();
        builder.setRequestId(requestId);
        builder.setProductCode(productCode);
        builder.setTopic(topic);
        builder.setPushType(Message.PushType.valueOf(pushType));
        builder.setDeviceId(deviceId);
        builder.setAccountId(accountId);
        return builder.build();
    }

    public static Message.Publish buildPublish(int requestId, int productCode, String topic, ByteString body, int pushType, String deviceId, String accountId, long expiry) {
        Message.Publish.Builder builder = Message.Publish.newBuilder();
        builder.setRequestId(requestId);
        builder.setProductCode(productCode);
        builder.setTopic(topic);
        builder.setBody(body);
        builder.setPushType(Message.PushType.valueOf(pushType));
        builder.setDeviceId(deviceId);
        builder.setAccountId(accountId);
        builder.setExpiry(expiry);
        return builder.build();
    }
}