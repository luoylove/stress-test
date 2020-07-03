package com.ly.core;

import com.google.common.collect.Lists;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: luoy
 * @Date: 2020/6/29 17:29.
 */
public class StressRemoteContext {
    private static StressResult totalResult = defaultWrap();

    public synchronized static void calculateResult(StressResult incrementalResult) {
        if (totalResult.getTotalCounter().get() == 0) {
            totalResult = incrementalResult;
        } else {
            totalResult.getTotalCounter().addAndGet(incrementalResult.getTotalCounter().get());
            totalResult.getFailedCounter().addAndGet(incrementalResult.getFailedCounter().get());
            totalResult.getEveryData().addAll(incrementalResult.getEveryData());
            totalResult.getEveryTimes().addAll(incrementalResult.getEveryTimes());
        }
    }

    public static StressResult get() {
        return totalResult;
    }

    public static StressResult defaultWrap() {
        return StressResult.builder().threadCount(0)
                .failedCounter(new AtomicInteger())
                .totalCounter(new AtomicInteger())
                .everyData(Lists.newArrayList())
                .everyTimes(Lists.newArrayList())
                .build();
    }
}
