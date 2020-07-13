package com.ly.core.tcp.client;

import com.ly.core.tcp.handler.NettyClientChannelHandler;
import com.ly.core.tcp.message.Invocation;
import com.ly.core.tcp.serialize.KryoDataDecoder;
import com.ly.core.tcp.serialize.KryoDataEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:59.
 */
public class NettyClient {

    public static final int READ_TIME_OUT = 5;

    private Channel channel;

    private final EventLoopGroup eventExecutors = new NioEventLoopGroup(1);

    private final Bootstrap client = new Bootstrap();

    public void start(String ip, int port) throws Exception {
        client.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline()
                                //空闲检查,触发IdleStateEvent事件,捕获后发送一次心跳
                                .addLast(new IdleStateHandler(READ_TIME_OUT, 0, 0))
                                .addLast("decoder", new KryoDataDecoder())
                                .addLast("encoder", new KryoDataEncoder())
                                .addLast("handler", new NettyClientChannelHandler());
                    }
                });
        ChannelFuture channelFuture = client.connect(ip, port).sync();

        if (!channelFuture.isSuccess()) {
            throw new RuntimeException("连接server服务器失败");
        }

        this.channel = channelFuture.channel();
    }

    public void send(Invocation invocation) {
        if (channel == null) {
            throw new RuntimeException("client端未连接");
        }
        channel.writeAndFlush(invocation);
    }

    public boolean isShutdown() {
        if (channel == null) {
            return true;
        }
        return !channel.isOpen();
    }

    public void shutdown() {
        channel.close();
        eventExecutors.shutdownGracefully();
        System.out.println("client关闭成功");
    }
}
