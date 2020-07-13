package com.ly.core.tcp.client;

import com.ly.core.tcp.handler.NettyClientChannelHandler;
import com.ly.core.tcp.message.Invocation;
import com.ly.core.tcp.serialize.KryoDataDecoder;
import com.ly.core.tcp.serialize.KryoDataEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:59.
 */
public class NettyClient {

    public static final int READ_TIME_OUT = 5;

    public static final Long RECONNECT_SECONDS = 60L;

    private Channel channel;

    private String ip;

    private int port;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private final EventLoopGroup eventExecutors = new NioEventLoopGroup(1);

    private final Bootstrap client = new Bootstrap();

    public void start() throws Exception {
        client.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .remoteAddress(ip, port)
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

        client.connect().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                System.out.println("连接server服务器失败, 开始重连");
                //重连
                reconnect();
                return;
            }
            this.channel = future.channel();
            System.out.println("连接server服务器成功, id: " + getChannelId());
        });
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

    public String getChannelId() {
        ChannelId id = channel.id();
        return id.asShortText();
    }

    public void shutdown() {
        String asShortText = getChannelId();
        channel.close();
        eventExecutors.shutdownGracefully();
        System.out.println("client关闭成功, id: " + asShortText);
    }

    public void reconnect() {
        this.eventExecutors.schedule(() -> {
            System.out.println("开始重连, id: " + getChannelId());
            try {
                start();
            } catch (Exception e) {
                System.out.println("重连失败 id: " + getChannelId());
                e.printStackTrace();
            }
        }, RECONNECT_SECONDS, TimeUnit.SECONDS);
    }
}
