package com.ly.core;

import com.google.common.collect.Lists;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: luoy
 * @Date: 2020/6/29 17:29.
 */
public class StressRemoteContext {
    private static StressResult totalResult = defaultWrap();

    public  static void calculateResult(StressResult incrementalResult) {
        synchronized( totalResult) {
            if (totalResult.getTotalCounter().get() == 0) {
                totalResult = incrementalResult;
            } else {
                totalResult.getTotalCounter().addAndGet(incrementalResult.getTotalCounter().get());
                totalResult.getFailedCounter().addAndGet(incrementalResult.getFailedCounter().get());
                totalResult.getEveryData().addAll(incrementalResult.getEveryData());
                totalResult.getEveryTimes().addAll(incrementalResult.getEveryTimes());
                totalResult.setThreadCount(incrementalResult.getThreadCount());
                if (incrementalResult.getTotalTime() != null && totalResult.getTotalTime() < incrementalResult.getTotalTime()) {
                    totalResult.setTotalTime(incrementalResult.getTotalTime());
                }
            }
        }
    }

    public static StressResult get() {
        return totalResult;
    }

    public static StressResult defaultWrap() {
        return StressResult.builder().threadCount(0)
                .failedCounter(new AtomicInteger())
                .totalCounter(new AtomicInteger())
                .everyData(Lists.newCopyOnWriteArrayList())
                .everyTimes(Lists.newCopyOnWriteArrayList())
                .totalTime(0L)
                .build();
    }
}
