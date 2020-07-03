package com.ly.core.tcp;

import com.ly.core.StressRequest;
import com.ly.core.tcp.serialize.KryoDataDecoder;
import com.ly.core.tcp.serialize.KryoDataEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:59.
 */
public class NettyClient {

    private Channel channel;

    private final EventLoopGroup eventExecutors = new NioEventLoopGroup(1);

    public void start(String ip, int port) throws Exception {
        Bootstrap client = new Bootstrap();
        client.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline()
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

    public void send(StressRequest request) {
        if (channel == null) {
            throw new RuntimeException("client端未连接");
        }
        channel.writeAndFlush(request);
    }

    public boolean isShutdown() {
        if (channel == null) {
            return true;
        }
        return !channel.isOpen();
    }

    public void shutdown() {
        eventExecutors.shutdownGracefully();
        System.out.println("client关闭成功");
    }
}
