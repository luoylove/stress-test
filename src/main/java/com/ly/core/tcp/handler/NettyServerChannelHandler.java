package com.ly.core.tcp.handler;

import com.google.common.collect.Lists;
import com.ly.StressTester;
import com.ly.core.StressContext;
import com.ly.core.StressRequest;
import com.ly.core.StressResult;
import com.ly.core.tcp.message.Invocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author: luoy
 * @Date: 2020/6/28 15:45.
 */
@Slf4j
public class NettyServerChannelHandler extends SimpleChannelInboundHandler<Invocation> {
    private StressTester tester = new StressTester();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation msg) throws Exception {
        //msg 接收客户端数据
        log.info("接收数据:{}", msg);
        if (msg.getType().equals(Invocation.Type.BUSINESS)) {
            StressRequest stressRequest = (StressRequest)msg.getMessage();
            if (stressRequest != null) {
                StressResult stressResult = tester.test(stressRequest);
                StressResult originalResult = null;

                //发送增量数据过去
                for(;;) {
                    TimeUnit.SECONDS.sleep(1);
                    StressResult incrementalResult;

                    if (!tester.getContext().isFinish()) {
                        incrementalResult = incrementalResult(originalResult, stressResult);
                        originalResult = clone(stressResult);
                        if (incrementalResult == null) {
                            continue;
                        }
                        Invocation invocation = Invocation.builder().type(Invocation.Type.BUSINESS).message(incrementalResult).build();
                        log.info("发送数据:{}", invocation);
                        ctx.writeAndFlush(invocation);
                    } else {
                        incrementalResult = incrementalResult(originalResult, stressResult);
                        if (incrementalResult != null) {
                            Invocation invocation = Invocation.builder().type(Invocation.Type.BUSINESS).message(incrementalResult).build();
                            log.info("发送数据:{}", invocation);
                            ctx.writeAndFlush(invocation);
                        }
                        Invocation invocation = Invocation.builder().type(Invocation.Type.DOWN).message(null).build();
                        log.info("发送数据:{}", invocation);
                        ctx.writeAndFlush(invocation);
                        break;
                    }
                }
            }
        } else if (msg.getType().equals(Invocation.Type.DOWN)) {
            StressContext context = tester.getContext();
            if (Objects.nonNull(context)) {
                context.setTimeStage(true);
            }
        } else {
            return;
        }
    }

    /**
     * 建立连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("address: {}", ctx.channel().remoteAddress());
    }

    /**
     * 发生异常
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // 断开连接
        ctx.channel().close();
    }


    private StressResult incrementalResult(StressResult originalResult, StressResult presentResult) {
        if (presentResult == null || presentResult.getTotalCounter().get() == 0) {
            return null;
        } else if (originalResult != null && originalResult.getTotalCounter().get() == presentResult.getTotalCounter().get()) {
            return null;
        } else if (originalResult == null) {
            return clone(presentResult);
        } else  {
            int originalSize = originalResult.getEveryTimes().size();
            int presentSize = presentResult.getEveryTimes().size();
            List<Long> everyTimes = Lists.newArrayListWithCapacity(presentSize - originalSize);
            List<Object> everyData = Lists.newArrayListWithCapacity(presentSize - originalSize);
            for(int i = originalSize; i < presentSize; i++ ) {
                everyTimes.add((Long) presentResult.getEveryTimes().get(i));
                everyData.add(presentResult.getEveryData().get(i));
            }
            return StressResult.builder().totalCounter(new AtomicInteger(presentResult.getTotalCounter().get() - originalResult.getTotalCounter().get()))
                    .failedCounter(new AtomicInteger(presentResult.getFailedCounter().get() - originalResult.getFailedCounter().get()))
                    .threadCount(presentResult.getThreadCount())
                    .everyTimes(everyTimes)
                    .totalTime(presentResult.getTotalTime())
                    .everyData(everyData)
                    .build();
        }
    }

    private StressResult clone(StressResult stressResult) {
        return StressResult.builder().threadCount(stressResult.getThreadCount())
                .failedCounter(new AtomicInteger(stressResult.getFailedCounter().get()))
                .totalCounter(new AtomicInteger(stressResult.getTotalCounter().get()))
                .everyData((List<Object>) stressResult.getEveryData().stream().collect(Collectors.toList()))
                .everyTimes((List<Long>) stressResult.getEveryTimes().stream().collect(Collectors.toList()))
                .totalTime(stressResult.getTotalTime())
                .build();
    }
}
