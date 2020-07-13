package com.ly.core.tcp.client;

import com.ly.core.tcp.handler.NettyClientChannelHandler;
import com.ly.core.tcp.serialize.KryoDataDecoder;
import com.ly.core.tcp.serialize.KryoDataEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Author: luoy
 * @Date: 2020/7/13 17:08.
 */
public class ClientChannelInitializer extends ChannelInitializer {

    private NettyClient nettyClient;

    public ClientChannelInitializer(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                //空闲检查,触发IdleStateEvent事件,捕获后发送一次心跳
                .addLast(new IdleStateHandler(NettyClient.READ_TIME_OUT, 0, 0))
                .addLast("decoder", new KryoDataDecoder())
                .addLast("encoder", new KryoDataEncoder())
                .addLast("handler", new NettyClientChannelHandler(nettyClient));
    }
}
