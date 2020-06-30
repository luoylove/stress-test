package com.ly.core;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: luoy
 * @Date: 2020/6/29 17:29.
 */
public class StressRemoteContext {
    public static Map<Thread, StressResult> remoteResult = new HashMap<>();

    public static StressResult totalResult =  StressResult.builder().everyData(Lists.newCopyOnWriteArrayList())
            .everyTimes(Lists.newCopyOnWriteArrayList())
            .failedCounter(new AtomicInteger())
            .totalCounter(new AtomicInteger())
            .threadCount(0)
            .build();
}
