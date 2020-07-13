package com.ly;

import com.ly.core.tcp.server.NettyServer;

/**
 * @Author: luoy
 * @Date: 2020/6/29 18:04.
 */
public class StressServerStart {
    private static final int PORT = 9998;

    public static void remoteServerStart() throws Exception {
        new NettyServer().start(PORT);
    }


    public static void main(String[] args) throws Exception {
        remoteServerStart();
    }
}
