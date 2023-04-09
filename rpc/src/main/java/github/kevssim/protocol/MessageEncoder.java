package github.kevssim.protocol;

import github.kevssim.protocol.message.Message;
import github.kevssim.serializer.JDKSerialization;
import github.kevssim.serializer.KryoSerialization;
import github.kevssim.serializer.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/*
    1. 标识协议以及版本号 2字节 高4位表示协议，低四位表示协议版本
    2. 指令类型 1字节
    3. 序列化类型 1字节
    4. requestId，请求唯一标识 8字节
    5. 正文长度 4字节
    6. 消息正文
 */

public class MessageEncoder extends MessageToByteEncoder<Message> {
    private static final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

    //编码Message，将其变为byte[]
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf out) throws Exception {
        //logger.info("this is encoder");
        //1.魔数
        out.writeBytes(new byte[]{1, 2});

        //2.指令类型 0：REQUEST 1:RESPONSE
        int messageType = message.getMessageType().getValue();
        out.writeByte(messageType);

        //3.序列化算法 0：JDK 1：Kyro
        int serializeType = message.getSerialType();
        out.writeByte(serializeType);

        //4.requestId
        out.writeLong(random.nextLong());

        //序列化，获取对象的字节数组
        Serialization serialization = null;
        if (serializeType == 0) {
            serialization = new JDKSerialization();
        } else if (serializeType == 1) {
            serialization = new KryoSerialization();
        } else {
            throw new RuntimeException("无法识别的序列化算法");
        }
        byte[] bytes = serialization.serialize(message);

        //5.正文长度
        out.writeInt(bytes.length);

        //6.消息正文
        out.writeBytes(bytes);
    }
}
