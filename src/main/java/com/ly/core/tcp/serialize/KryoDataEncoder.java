package com.ly.core.tcp.serialize;

import com.ly.core.tcp.message.Invocation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: luoy
 * @Date: 2020/6/30 10:08.
 */
public class KryoDataEncoder extends MessageToByteEncoder<Invocation> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Invocation msg, ByteBuf out) throws Exception {
        KryoCodec.encode(out, msg);
        ctx.flush();
    }
}