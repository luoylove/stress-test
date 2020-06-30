package com.ly.core.tcp;

import com.ly.core.tcp.serialize.StressDataDecoder;
import com.ly.core.tcp.serialize.StressDataEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:41.
 */
public class NettyServer {

    public void start(int port) throws Exception{
        EventLoopGroup parentGroup = new NioEventLoopGroup(1);
        EventLoopGroup childGroup = new NioEventLoopGroup(1);
        try{
            ServerBootstrap serverBoot = new ServerBootstrap();
            serverBoot.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("decoder", new StressDataDecoder())
                                    .addLast("encoder", new StressDataEncoder())
                                    .addLast("handler", new NettyServerChannelHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBoot.bind(port).sync();
            if(!channelFuture.isSuccess()){
                throw new RuntimeException("Server服务启动失败");
            }
            System.out.println(("Server服务启动成功"));
            channelFuture.channel().closeFuture().sync();
        } finally {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
