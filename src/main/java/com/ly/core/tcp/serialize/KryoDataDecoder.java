package com.ly.core.tcp.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: luoy
 * @Date: 2020/6/30 10:14.
 */
public class KryoDataDecoder extends ByteToMessageDecoder {
    final public static int MESSAGE_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        //出现粘包导致消息头长度不对，直接返回
        if (in.readableBytes() < MESSAGE_LENGTH) {
            return;
        }

        in.markReaderIndex();
        //读取消息的内容长度
        int messageLength = in.readInt();

        if (messageLength < 0) {
            ctx.close();
        }

        //读到的消息长度和报文头的已知长度不匹配。那就重置一下ByteBuf读索引的位置
        if (in.readableBytes() < messageLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[messageLength];
        in.readBytes(bytes);
        Object obj = KryoCodec.decode(bytes);
        out.add(obj);
    }
}
