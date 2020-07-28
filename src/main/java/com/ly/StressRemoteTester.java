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

/**
 * 远程压测
 * @Author: luoy
 * @Date: 2020/6/28 14:30.
 */
public class StressRemoteTester {

    public static void remoteTest(StressRequest request, String...addresses) throws Exception {
        NettyClientManager manager = NettyClientManager.getInstance();
        Invocation invocation = Invocation.builder().message(request).type(Invocation.Type.BUSINESS).build();
        for(String address : addresses) {
            String[] add = StringUtils.split(address, ":");
            NettyClient nettyClient = new NettyClient(add[0], Integer.valueOf(add[1]));
            nettyClient.start();
            manager.add(nettyClient);
        }

        manager.sendAll(invocation);
    }

    public static void main(String[] args) throws Exception {
        List<StressTask<String>> tasks = Lists.newArrayList(new LogTask("1"), new LogTask("2"), new LogTask("3"), new LogTask("4"), new LogTask("5"), new LogTask("6"), new LogTask("7"));
        StressRequest<String> stressRequest = StressRequest.<String>builder().tasks(tasks)
                .threadCount(10)
                .concurrencyCount(10)
                .totalConcurrencyTime(10L * 1000)
                .build();
        StressRemoteTester.remoteTest(stressRequest, "localhost:9998", "106.54.36.74:9998");
//                StressRemoteTester.remoteTest(stressRequest, "106.54.36.74:9998");

        for(;;) {
            StressFormat.format(StressRemoteContext.get());
            if (NettyClientManager.getInstance().isFinish()) {
                ThreadPoolUtil.shutdown();
                return;
            }
            Thread.sleep(1000);
        }
    } 
}
