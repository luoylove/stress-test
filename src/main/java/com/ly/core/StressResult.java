package com.ly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 完成后结果保存类
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StressResult<T> implements Serializable {
    /** 统计任务执行失败数量 */
    private AtomicInteger failedCounter;

    /** 执行总数 **/
    private AtomicInteger totalCounter;

    /**
     *  每一个请求的时间间隔 ns
     */
    private List<Long> everyTimes;

    /**
     * 每一个请求的返回值
     */
    private List<T> everyData;

    /**
     * 线程数量
     */
    private int threadCount;
}
