package github.kevssim.protocol;

import github.kevssim.protocol.message.Message;
import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import github.kevssim.serializer.JDKSerialization;
import github.kevssim.serializer.KryoSerialization;
import github.kevssim.serializer.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
    1. 魔数，标识协议以及版本号 2字节 高4位表示协议，低四位表示协议版本
    2. 指令类型 1字节
    3. 序列化类型 1字节
    4. requestId，请求唯一标识 8字节
    5. 正文长度 4字节
    6. 消息正文
 */

public class MessageDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    //解码，将byte变为Message对象
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //logger.info("this is decoder");

        byte protocol = in.readByte();
        byte protocolVersion = in.readByte();
        //TODO:校验协议以及协议版本
        byte messageType = in.readByte();
        byte serializeType = in.readByte();
        long requestId = in.readLong();
        int length = in.readInt();

        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        //反序列化
        Serialization serialization = null;
        if (serializeType == 0) {
            serialization = new JDKSerialization();
        } else if (serializeType == 1) {
            serialization = new KryoSerialization();
        } else {
            throw new RuntimeException("无法识别的序列化算法");
        }

        Message message = null;
        if (messageType == (byte) 0) {
            message = serialization.deserialize(bytes, RpcRequest.class);
        } else if (messageType == (byte) 1) {
            message = serialization.deserialize(bytes, RpcResponse.class);
        }

        //结果加入list
        out.add(message);
    }
}
