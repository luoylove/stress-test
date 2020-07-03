package com.ly;

import com.google.common.collect.Lists;
import com.ly.core.StressRequest;
import com.ly.core.StressTask;
import com.ly.core.taskimpl.LogTask;
import com.ly.core.tcp.NettyClient;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 远程压测
 * @Author: luoy
 * @Date: 2020/6/28 14:30.
 */
public class StressRemoteTester {

    public static void remoteTest(StressRequest request, String...addresses) throws Exception {
        NettyClient nettyClient = new NettyClient();
        for(String address : addresses) {
            String[] add = StringUtils.split(address, ":");
            nettyClient.start(add[0], Integer.valueOf(add[1]));
            nettyClient.send(request);
        }

//        ThreadPoolUtil.execute(() -> {
//            while (true) {
//                System.out.println("数据合并....");
//                if (StressRemoteContext.remoteResult.size() == 1) {
//                    StressRemoteContext.totalResult = StressRemoteContext.remoteResult.get(0);
//                } else {
//                    AtomicInteger failedCounter = new AtomicInteger();
//                    AtomicInteger totalCounter = new AtomicInteger();
//                    AtomicInteger threadCount = new AtomicInteger();
//                    CopyOnWriteArrayList<Long> everyTimes = Lists.newCopyOnWriteArrayList();
//                    CopyOnWriteArrayList everyData = Lists.newCopyOnWriteArrayList();
//
//                    StressRemoteContext.remoteResult.forEach((k, v) -> {
//                        failedCounter.addAndGet(v.getFailedCounter().get());
//                        totalCounter.addAndGet(v.getTotalCounter().get());
//                        threadCount.addAndGet(v.getThreadCount());
//                        everyTimes.addAll(v.getEveryTimes());
//                        everyData.addAll(v.getEveryData());
//                    });
//
//                    StressRemoteContext.totalResult.setThreadCount(threadCount.get());
//                    StressRemoteContext.totalResult.setFailedCounter(failedCounter);
//                    StressRemoteContext.totalResult.setTotalCounter(totalCounter);
//                    StressRemoteContext.totalResult.setEveryTimes(everyTimes);
//                    StressRemoteContext.totalResult.setEveryData(everyData);
//                }
//                if (nettyClient.isShutdown()) {
//                    nettyClient.shutdown();
//                    System.out.println("退出....");
//                    return;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public static void main(String[] args) throws Exception {
        List<StressTask<String>> tasks = Lists.newArrayList(new LogTask("1"), new LogTask("2"), new LogTask("3"), new LogTask("4"), new LogTask("5"), new LogTask("6"), new LogTask("7"));
        StressRequest<String> stressRequest = StressRequest.<String>builder().tasks(tasks).threadCount(10).totalConcurrencyTime(10L * 1000).build();

        StressRemoteTester.remoteTest(stressRequest, "localhost:9998");

//        for(;;) {
//            System.out.println("format");
//            System.out.println(StressRemoteContext.totalResult.toString());
////            StressFormat.format(StressRemoteContext.totalResult);
//            Thread.sleep(1000);
//        }
    }
}
