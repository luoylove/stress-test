package com.ly.core.tcp.serialize;

import com.ly.core.tcp.message.Invocation;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author: luoy
 * @Date: 2020/7/2 13:56.
 */
public class KryoCodec {

    public static void encode(final ByteBuf out, final Invocation message) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            KryoSerialize kryoSerialization = new KryoSerialize(KryoPoolFactory.getKryoPoolInstance());
            kryoSerialization.serialize(byteArrayOutputStream, message);
            byte[] body = byteArrayOutputStream.toByteArray();
            int dataLength = body.length;
            out.writeInt(dataLength);
            out.writeBytes(body);
        }
    }

    public static Object decode(byte[] body) throws IOException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body)) {
            KryoSerialize kryoSerialization = new KryoSerialize(KryoPoolFactory.getKryoPoolInstance());
            Object obj = kryoSerialization.deserialize(byteArrayInputStream);
            return obj;
        }
    }
}
