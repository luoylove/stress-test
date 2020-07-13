package com.ly.core.tcp.handler;

import com.ly.core.StressRemoteContext;
import com.ly.core.StressResult;
import com.ly.core.tcp.client.NettyClient;
import com.ly.core.tcp.message.Invocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:45.
 */
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<Invocation> {

    private static boolean isDown = false;

    private NettyClient nettyClient;

    public NettyClientChannelHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation msg) throws Exception {
        //msg 接收服务端数据
        System.out.println("接收数据:" + msg);
        switch (msg.getType()) {
            case BUSINESS:
                doBusiness(msg);
                break;
            case DOWN:
                doDown(ctx);
                break;
            case HEARTBEAT:
                doHeartbeat(msg);
                break;
            case AUTH:
                default:
                    break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //Netty 提供了 IdleStateHandler 处理器，提供空闲检测的功能，在 Channel 的读或者写空闲时间太长时，将会触发一个 IdleStateEvent 事件
        if (evt instanceof IdleStateEvent) {
            Invocation invocation = Invocation.builder().type(Invocation.Type.HEARTBEAT).build();
            System.out.println("heartbeat send: " + invocation);
            ctx.writeAndFlush(invocation);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 如果不是手动断开连接就发起重连
        if (!isDown) {
            nettyClient.reconnect();
        }

        // 继续触发事件
        super.channelInactive(ctx);
    }

    private void doDown(ChannelHandlerContext ctx) {
        isDown = true;
        ctx.channel().close();
        ctx.disconnect();
        ctx.close();
        System.out.println("client退出");
        return;
    }

    private void doBusiness(Invocation msg) {
        StressResult remoteResult = (StressResult) msg.getMessage();
        if (remoteResult.getTotalCounter().get() <= 0) {
            return;
        }
        StressRemoteContext.calculateResult(remoteResult);
    }

    private void doHeartbeat(Invocation msg) {
    }
}
