package com.ly.core.tcp;

import com.ly.StressTester;
import com.ly.core.StressRequest;
import com.ly.core.StressResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:45.
 */
public class NettyServerChannelHandler extends SimpleChannelInboundHandler<Object> {

    public static final String DOWN_FLAG = "STRESS-SERVER-DOWN";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //msg 接收客户端数据
        System.out.println("接收数据:" + msg);
        if (msg instanceof StressRequest) {
            StressRequest stressRequest = (StressRequest)msg;
            if (stressRequest != null) {
                StressTester tester = new StressTester();
                StressResult stressResult = tester.test(stressRequest);

                for(;;) {
                    Thread.sleep(1000);
//                    System.out.println("发送数据: " + stressResult);
                    if (!tester.getFinish()) {
                        ctx.writeAndFlush(stressResult);
                    } else {
                        ctx.writeAndFlush(stressResult);
                        ctx.writeAndFlush(DOWN_FLAG);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 建立连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("address:" + ctx.channel().remoteAddress());
    }
}
