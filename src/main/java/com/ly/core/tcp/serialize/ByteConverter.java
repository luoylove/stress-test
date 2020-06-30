package com.ly.core.tcp.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @Author: luoy
 * @Date: 2020/6/30 9:54.
 */
public class ByteConverter {
    public static byte[] encode(Object obj) {
        byte[] bytes = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            bytes = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static Object decode(byte[] bytes) {
        Object obj = null;

        try(ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream inn = new ObjectInputStream(in)) {
            obj = inn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static ByteBuf byteToByteBuf (byte[] bytes) {
        return Unpooled.copiedBuffer(bytes);
    }

    public static byte[] byteBufToByte(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
