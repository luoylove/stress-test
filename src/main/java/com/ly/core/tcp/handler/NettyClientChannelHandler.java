package com.ly.core.tcp.handler;

import com.ly.core.tcp.client.NettyClient;
import com.ly.core.tcp.client.NettyClientManager;
import com.ly.core.tcp.message.Invocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:45.
 */
@Slf4j
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<Invocation> {

    private NettyClient nettyClient;

    public NettyClientChannelHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    private static NettyClientManager manager = NettyClientManager.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation msg) throws Exception {
         manager.read(ctx.channel().id().asShortText(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //Netty 提供了 IdleStateHandler 处理器，提供空闲检测的功能，在 Channel 的读或者写空闲时间太长时，将会触发一个 IdleStateEvent 事件
        if (evt instanceof IdleStateEvent) {
            Invocation invocation = Invocation.builder().type(Invocation.Type.HEARTBEAT).build();
            log.info("[{}]heartbeat send: {}",ctx.channel().id().asShortText(), invocation);
            manager.send(ctx.channel().id().asShortText(), invocation);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 在客户端和服务端断开连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        manager.removeAndClose(ctx.channel().id().asShortText());
        ctx.fireChannelUnregistered();
    }

    /**
     * 建立连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[{}]建立连接", ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isOpen()) {
            nettyClient.reconnect();
        }

        // 继续触发事件
        super.channelInactive(ctx);
    }
}
