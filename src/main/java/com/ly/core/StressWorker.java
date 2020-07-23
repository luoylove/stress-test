package com.ly.core;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程主体
 *
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
public class StressWorker<T> implements Runnable{
    private List<StressTask> stressTasks;

    private StressContext stressContext;

    private List<Long> localTime;

    private List<Object> localData;

    private AtomicInteger localTotalCount;

    /**内部只有一个线程在执行任务, list线程安全
     * CopyOnWriteArrayList是一个写线程安全list,
     * 虽然比Collections.synchronizedList()读写全加锁性能好, 但是写线程安全实现模式是copy原来的list,
     * 当队列过长, 有oom风险,且每个线程竞争同步list有性能损耗
     * 所有内部维护一个同步list,通过辅助线程进行同步到context中,只有该工作线程与辅助线程竞争
     * 同时辅助线程进行list清除,防止内存占用过多
     * */
    public StressWorker(List<StressTask> stressTasks, StressContext stressContext) {
        this.stressTasks = stressTasks;
        this.stressContext = stressContext;
        this.localTime = Lists.newCopyOnWriteArrayList();
        this.localData = Lists.newCopyOnWriteArrayList();
        this.localTotalCount = new AtomicInteger();
    }

    public List<Long> getLocalTime() {
        return localTime;
    }

    public Long getLocalTimeSum() {
        return localTime.stream().mapToLong(x -> x).sum();
    }

    public List<Object> getLocalData() {
        return localData;
    }

    public int getLocalTotalCount() {
        return localTotalCount.get();
    }

    @Override
    public void run() {
        try {
            //设置集合点栅栏
            stressContext.getStartBarrier().await();
            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //线程执行完成后哨兵计数器-1
            stressContext.getEndLatch().countDown();
        }
    }


    private void doRun() {
        while (true) {
            for(StressTask<T> stressTask : stressTasks) {
                if(stressContext.isTimeStage()) {
                    return;
                }
                Long endTime = null;
                T res = null;
                Boolean isFailed = false;
                Long startTime = System.nanoTime() ;
                try {
                    res =  stressTask.task();
                } catch (Throwable throwable) {
                    endTime = System.nanoTime();
                    throwable.printStackTrace();
                    //如果执行失败把错误保存
                    localData.add(throwable);
                    isFailed = true;
                } finally {
                    Long everyTime;
                    if (!isFailed) {
                        everyTime = System.nanoTime() - startTime;
                        localData.add(res);
                    } else {
                        everyTime = endTime - startTime;
                    }
                    localTotalCount.getAndIncrement();
                    localTime.add(everyTime);
                }
            }
            if (stressContext.isCountStage()) {
                return;
            }
        }
    }
}
