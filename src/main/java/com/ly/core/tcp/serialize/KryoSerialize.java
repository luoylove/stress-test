package com.ly.core.tcp.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @Author: luoy
 * @Date: 2020/7/2 13:52.
 */
public class KryoSerialize implements Serializable {

    private KryoPool pool;

    private static Closer closer = Closer.create();

    public KryoSerialize(final KryoPool pool) {
        this.pool = pool;
    }

    public void serialize(OutputStream output, Object object) throws IOException {
        Kryo kryo = pool.borrow();
        Output out = new Output(output);
        closer.register(out);
        closer.register(output);
        kryo.writeClassAndObject(out, object);
        pool.release(kryo);
        closer.close();
    }

    public Object deserialize(InputStream input) throws IOException{
        Kryo kryo = pool.borrow();
        Input in = new Input(input);
        Object result = kryo.readClassAndObject(in);
        closer.register(input);
        closer.register(in);
        pool.release(kryo);
        closer.close();
        return result;
    }
}
