package com.ly.core.tcp.server;

import com.ly.core.tcp.handler.NettyServerChannelHandler;
import com.ly.core.tcp.serialize.KryoDataDecoder;
import com.ly.core.tcp.serialize.KryoDataEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:41.
 */
@Slf4j
public class NettyServer {
    private EventLoopGroup parentGroup = new NioEventLoopGroup(1);

    private EventLoopGroup childGroup = new NioEventLoopGroup(1);

    private Channel channel;

    public void start(int port) throws Exception{

        try{
            ServerBootstrap serverBoot = new ServerBootstrap();
            serverBoot.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("decoder", new KryoDataDecoder())
                                    .addLast("encoder", new KryoDataEncoder())
                                    .addLast("handler", new NettyServerChannelHandler());
                        }
                    })
                    //理论上来说服务端就一个客户端连上来发送脚本 accept 队列的大小可设置小点
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //心跳
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBoot.bind(port).sync();
            if(!channelFuture.isSuccess()){
                log.error("Server服务启动失败, 端口: {}", port);
            }
            log.info("Server服务启动成功, 端口: {}", port);
            channelFuture.channel().closeFuture().sync();
            channel = channelFuture.channel();
        } finally {
            if (channel != null) {
                channel.close();
            }
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
