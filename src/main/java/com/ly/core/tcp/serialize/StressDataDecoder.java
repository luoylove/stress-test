package com.ly.core.tcp.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: luoy
 * @Date: 2020/6/30 10:14.
 */
public class StressDataDecoder extends ByteToMessageDecoder{
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = ByteConverter.byteBufToByte(in);
        Object obj = ByteConverter.decode(bytes);
        out.add(obj);
    }
}
