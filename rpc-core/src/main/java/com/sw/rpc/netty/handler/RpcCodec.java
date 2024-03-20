package com.sw.rpc.netty.handler;

import com.sw.rpc.dto.Message;
import com.sw.rpc.assist.serialize.SerializeHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.sw.rpc.constant.Constant.MESSAGE_MAGIC_NUM;
import static com.sw.rpc.constant.Constant.MessageVersion.MESSAGE_VERSION_1;
import static com.sw.rpc.dto.Message.messageClassMap;
import static com.sw.rpc.assist.serialize.SerializeHelper.SERIALIZE_TYPE_JSON;

@Slf4j
@ChannelHandler.Sharable
public class RpcCodec extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1. 魔数 (4 Byte)
        out.writeInt(MESSAGE_MAGIC_NUM);
        // 2. 版本 (1 Byte)
        out.writeByte(MESSAGE_VERSION_1.getVersion());
        // 3. 序列化方式 (1 Byte)：1-jdk，2-json
        out.writeByte(SERIALIZE_TYPE_JSON);
        // 4. 消息类型 (1 Byte)
        out.writeByte(msg.messageType());
        // 5.无意思，仅用于非内容字段的对齐填充，使其符合 2^n 大小
        out.writeByte(0);
        // 获取内容
        byte[] byteArray = SerializeHelper.serialize(SERIALIZE_TYPE_JSON, msg);
        // 6. 内容长度 (4 Byte)
        out.writeInt(byteArray.length);
        // 7. 内容正文
        out.writeBytes(byteArray);

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> outList) throws Exception {
        // 1. 魔数 (4 Byte)
        int magicNum = in.readInt();
        // 2. 版本 (1 Byte)
        byte version = in.readByte();
        checkMagicNumAndVersion(magicNum, (int) version);
        // 3. 序列化方式 (1 Byte)：0-jdk，1-json
        Byte serializeType = in.readByte();
        // 4. 消息类型 (1 Byte)
        Byte messageType = in.readByte();
        // 5.无意思，仅用于非内容字段的对齐填充，使其符合 2^n 大小
        in.readByte();
        // 6. 内容长度 (4 Byte)
        int length = in.readInt();
        // 7. 内容正文
        byte[] content = new byte[length];
        in.readBytes(content, 0, length);

        Object message = SerializeHelper.deserialize((int) serializeType, messageClassMap.get((int) messageType), content);
        outList.add(message);
    }

    private void checkMagicNumAndVersion(int magicN, int vers) {
        if (magicN != MESSAGE_MAGIC_NUM || vers != MESSAGE_VERSION_1.getVersion()) {
            log.error("magicNum or version not support");
            throw new RuntimeException("magicNum or version not support");
        }
    }

}
