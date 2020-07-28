package com.ly.core.tcp.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @Author: luoy
 * @Date: 2020/7/2 13:52.
 */
public class KryoSerialize implements Serializable {

    private KryoPool pool;

    public KryoSerialize(final KryoPool pool) {
        this.pool = pool;
    }

    public void serialize(OutputStream output, Object object) {
        try(Output out = new Output(output)) {
            Kryo kryo = pool.borrow();
            kryo.writeClassAndObject(out, object);
            pool.release(kryo);
        }
    }

    public Object deserialize(InputStream input){
        try(Input in = new Input(input)) {
            Kryo kryo = pool.borrow();
            Object result = kryo.readClassAndObject(in);
            pool.release(kryo);
            return result;
        }
    }
}
