package com.ly.core.tcp.client;

import com.ly.core.tcp.message.Invocation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:59.
 */
@Slf4j
public class NettyClient {

    /** 心跳时间*/
    public static final int READ_TIME_OUT = 60;
    /**重试初始延迟*/
    public static final Long RECONNECT_DELAY_SECONDS = 3L;

    private Channel channel;

    private String ip;

    private int port;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Channel getChannel() {
        return this.channel;
    }

    private final EventLoopGroup eventExecutors = new NioEventLoopGroup(1);

    public void start() throws Exception {
        Bootstrap client = new Bootstrap();
        client.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .remoteAddress(ip, port)
                .handler(new ClientChannelInitializer(this));

        client.connect().addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //重连
                reconnect();
                return;
            }
            this.channel = future.channel();
            log.info("[{}]连接服务器成功", getAddress());
        });
    }

    public String getAddress() {
        return ip + ":" + String.valueOf(port);
    }

    public void send(Invocation invocation) {
        channel.writeAndFlush(invocation);
    }

    /** 是否活跃*/
    public boolean isActive() {
        if (channel ==null ) {
            return false;
        }
        return channel.isActive();
    }

    public boolean isShutdown() {
        if (channel == null) {
            return true;
        }
        return !channel.isOpen();
    }

    public void shutdown() {
        if(!isShutdown()) {
            channel.close();
        }
        eventExecutors.shutdownGracefully();
        log.info("[{}]断开连接", getAddress());
    }

    public void reconnect() {
        this.eventExecutors.schedule(() -> {
            log.info("[{}]开始重连", getAddress());
            try {
                start();
            } catch (Exception e) {
                log.info("[{}]重连失败", getAddress());
                e.printStackTrace();
            }
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
        log.info("[{}]连接服务器失败, {}秒后将发起重连", getAddress(), RECONNECT_DELAY_SECONDS);
    }
}
