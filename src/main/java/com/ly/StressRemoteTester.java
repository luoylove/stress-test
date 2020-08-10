package com.ly;

import com.google.common.collect.Lists;
import com.ly.core.StressFormat;
import com.ly.core.StressRemoteContext;
import com.ly.core.StressRequest;
import com.ly.core.StressTask;
import com.ly.core.taskimpl.LogTask;
import com.ly.core.tcp.client.NettyClient;
import com.ly.core.tcp.client.NettyClientManager;
import com.ly.core.tcp.message.Invocation;
import com.ly.core.util.ThreadPoolUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 远程压测
 * @Author: luoy
 * @Date: 2020/6/28 14:30.
 */
public class StressRemoteTester {

    public static void remoteTest(StressRequest request, String...addresses) throws Exception {
        NettyClientManager manager = NettyClientManager.getInstance();
        Invocation invocation = Invocation.builder().message(request).type(Invocation.Type.BUSINESS).build();
        connectAll(addresses);
        manager.syncSendAll(invocation);
    }

    /** 停止远程压测机器正在执行的压测脚本**/
    public static void remoteStop(String...addresses) throws Exception{
        NettyClientManager manager = NettyClientManager.getInstance();
        Invocation invocation = Invocation.builder().type(Invocation.Type.DOWN).build();
        connectAll(addresses);
        manager.syncSendAll(invocation);
        manager.shutdownAll();
    }

    private static void connectAll(String...addresses) throws Exception {
        NettyClientManager manager = NettyClientManager.getInstance();
        for(String address : addresses) {
            String[] add = StringUtils.split(address, ":");
            NettyClient nettyClient = new NettyClient(add[0], Integer.valueOf(add[1]));
            nettyClient.start();
            manager.add(nettyClient);
        }
    }

    public static void main(String[] args) throws Exception {
        List<StressTask<String>> tasks = Lists.newArrayList(new LogTask("1"), new LogTask("2"), new LogTask("3"), new LogTask("4"), new LogTask("5"), new LogTask("6"), new LogTask("7"));
        StressRequest<String> stressRequest = StressRequest.<String>builder().tasks(tasks)
                .threadCount(10)
                .totalConcurrencyTime(10L * 1000)
                .build();
        StressRemoteTester.remoteTest(stressRequest, "localhost:9998");

        for(;;) {
            StressFormat.format(StressRemoteContext.get());
            if (NettyClientManager.getInstance().isFinish()) {
                ThreadPoolUtil.shutdown();
                return;
            }
            TimeUnit.SECONDS.sleep(1);
        }
    } 
}
