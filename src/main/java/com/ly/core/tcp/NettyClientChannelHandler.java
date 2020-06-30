package com.ly.core.tcp;

import com.ly.core.StressRemoteContext;
import com.ly.core.StressResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:45.
 */
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg);

        if (msg instanceof String) {
            if (NettyServerChannelHandler.DOWN_FLAG.equals(msg)) {
                ctx.disconnect();
                return;
            }
        }

        if (msg instanceof StressResult) {
            StressResult remoteResult = (StressResult) msg;
            if (remoteResult != null) {
                StressRemoteContext.remoteResult.put(Thread.currentThread(), remoteResult);
            }
        }
    }
}
