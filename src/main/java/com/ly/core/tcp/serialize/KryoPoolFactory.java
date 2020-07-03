package com.ly.core.tcp.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.ly.core.StressRequest;
import com.ly.core.StressResult;
import com.ly.core.StressTask;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.List;

/**
 * @Author: luoy
 * @Date: 2020/7/2 13:54.
 */
public class KryoPoolFactory {
    private static KryoPoolFactory poolFactory = null;

    private KryoPoolFactory() {}

    private KryoFactory factory = () -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
//        //把已知的结构注册到Kryo注册器里面，提高序列化/反序列化效率
        kryo.register(StressResult.class);
        kryo.register(StressRequest.class);
        kryo.register(List.class);
        kryo.register(StressTask.class);
        kryo.register(String.class);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        return kryo;
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    public static KryoPool getKryoPoolInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}
