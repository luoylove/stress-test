package com.ly.core.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时线程池
 */
public class ScheduledThreadPoolUtil {
    private static final int CORE_POOL_SIZE =  5;

    /** 监听剔除定时任务间隔时间*/
    private static final long REJECT_FUTURE_INTERVAL_TIME = 100L;

    private static volatile ScheduledThreadPoolExecutor THREAD_POOL;

    private static ScheduledThreadPoolExecutor getInstance() {
        if (THREAD_POOL == null) {
            synchronized (ScheduledThreadPoolUtil.class) {
                if (THREAD_POOL == null || THREAD_POOL.isShutdown()) {
                    THREAD_POOL = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE,
                                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(false).build());
                }
            }
        }
        return THREAD_POOL;
    }

    /**
     * 固定间隔延迟执行,适合执行时间比“间隔”短的任务
     * 即下一次任务开始的时间为：上一次任务开始时间 + period时间
     * 如果任务执行的时间比period长的话，会导致该任务延迟执行，不会同时执行！
     * 如果任务执行过程抛出异常，后续不会再执行该任务！
     *
     * @param command
     *              执行体
     * @param initialDelay
     *              初始延迟执行时间
     * @param period
     *              开始时间的间隔为period，即“固定间隔”执行
     * @param executionTotal
     *              定时任务执行时长
     * @param unit
     *              时间单位
     */
    public static void scheduleAtFixedRateByTime(Runnable command, long initialDelay, long period, long executionTotal, TimeUnit unit) {
        ScheduledThreadPoolExecutor poolExecutor = ScheduledThreadPoolUtil.getInstance();
        ScheduledFuture<?> scheduledFuture = poolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
        Long start = System.nanoTime();
        for(;;) {
            try {
                TimeUnit.MILLISECONDS.sleep(REJECT_FUTURE_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.nanoTime() - start >= unit.toNanos(executionTotal)) {
                poolExecutor.remove((Runnable) scheduledFuture);
                break;
            }
        }
    }

    /**
     * 固定间隔延迟执行,适合执行时间比“间隔”长的任务
     * 在每一次执行终止和下一次执行开始之间都存在给定的延迟delay时间
     * 即下一次任务开始的时间为：上一次任务结束时间（而不是开始时间） + delay时间
     * 如果任务执行过程抛出异常，后续不会再执行该任务！
     *
     * @param command
     *              执行体
     * @param initialDelay
     *              初始延迟执行时间
     * @param delay
     *              开始时间的间隔为period，即“固定间隔”执行
     * @param executionTotal
     *              定时任务执行时长
     * @param unit
     *              时间单位
     */
    public static void scheduleWithFixedDelayByTime(Runnable command, long initialDelay, long delay, long executionTotal, TimeUnit unit) {
        ScheduledThreadPoolExecutor poolExecutor = ScheduledThreadPoolUtil.getInstance();
        ScheduledFuture<?> scheduledFuture = poolExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);

        Long start = System.nanoTime();
        for(;;) {
            try {
                TimeUnit.MILLISECONDS.sleep(REJECT_FUTURE_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.nanoTime() - start >= unit.toNanos(executionTotal)) {
                scheduledFuture.cancel(true);
                break;
            }
        }
    }

    /**
     * 固定间隔延迟执行
     * @param command
     *              执行体
     * @param initialDelay
     *              初始延迟执行时间
     * @param delay
     *              开始时间的间隔为period，即“固定间隔”执行
     * @param number
     *              执行多少次后退出
     * @param unit
     */
    public static void scheduleDelayByNumber(Runnable command, long initialDelay, long delay, long number, TimeUnit unit) {
        ScheduledThreadPoolExecutor poolExecutor = ScheduledThreadPoolUtil.getInstance();
        long everyDelay = initialDelay;
        for (int i = 0; i < number; i++) {
            poolExecutor.schedule(command, everyDelay, unit);
            everyDelay += delay;
        }
    }

    /**
     * 根据computable 计算状态是否退出, Computable.compute()返回 true 退出
     * 适合执行时间比“间隔”短的任务
     * @param command
     *              执行体
     * @param initialDelay
     *              初始延迟执行时间
     * @param period
     *              开始时间的间隔为period，即“固定间隔”执行
     * @param computable
     *              计算属性, 是一个随时间可变属性
     * @param unit
     */
    public static void scheduleAtFixedRateByCompute(Runnable command, long initialDelay, long period, Computable computable, TimeUnit unit) {
        ScheduledThreadPoolExecutor poolExecutor = ScheduledThreadPoolUtil.getInstance();
        ScheduledFuture<?> scheduledFuture = poolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
        for(;;) {
            try {
                TimeUnit.MILLISECONDS.sleep(REJECT_FUTURE_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (computable.compute()) {
                poolExecutor.remove((Runnable) scheduledFuture);
                break;
            }
        }
    }

    /**
     * 根据computable 计算状态是否退出, Computable.compute()返回 true 退出
     * 适合执行时间比“间隔”长的任务
     * @param command
     *              执行体
     * @param initialDelay
     *              初始延迟执行时间
     * @param period
     *              开始时间的间隔为period，即“固定间隔”执行
     * @param computable
     *              计算属性, 是一个随时间可变属性
     * @param unit
     */
    public static void scheduleWithFixedDelayByCompute(Runnable command, long initialDelay, long period, Computable computable, TimeUnit unit) {
        ScheduledThreadPoolExecutor poolExecutor = ScheduledThreadPoolUtil.getInstance();
        ScheduledFuture<?> scheduledFuture = poolExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
        for(;;) {
            try {
                TimeUnit.MILLISECONDS.sleep(REJECT_FUTURE_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (computable.compute()) {
                poolExecutor.remove((Runnable) scheduledFuture);
                break;
            }
        }
    }

    /**
     * 延迟执行一遍
     * @param command
     * @param delay
     * @param unit
     * @return
     */
    public static ScheduledFuture<?> schedule(Runnable command,
                                long delay,
                                TimeUnit unit) {
        return ScheduledThreadPoolUtil.getInstance().schedule(command, delay, unit);
    }

    /**
     * 延迟执行一遍
     * @param callable
     * @param delay
     * @param unit
     * @param <V>
     * @return
     */
    public static <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                long delay,
                                TimeUnit unit) {
        return ScheduledThreadPoolUtil.getInstance().schedule(callable, delay, unit);
    }

    public static void shutdown() {
        ScheduledThreadPoolUtil.getInstance().shutdown();
    }
}