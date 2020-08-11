package com.ly;

import com.google.common.collect.Lists;
import com.ly.core.AsyncStressResultHandler;
import com.ly.core.StressContext;
import com.ly.core.StressFormat;
import com.ly.core.StressRequest;
import com.ly.core.StressResult;
import com.ly.core.StressTask;
import com.ly.core.StressThreadPool;
import com.ly.core.StressWorker;
import com.ly.core.enums.ValidateRule;
import com.ly.core.enums.ValidateTarget;
import com.ly.core.taskimpl.LogTask;
import com.ly.core.util.ScheduledThreadPoolUtil;
import com.ly.core.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: luoy
 * @Date: 2020/6/16 16:06.
 */
@Slf4j
public class StressTester {

    public StressContext getContext() {
        return context;
    }

    private StressContext context;

    public StressResult test(StressRequest request) {
        this.context = StressContext.builder().startBarrier(new CyclicBarrier(request.getThreadCount()))
                .endLatch(new CountDownLatch(request.getThreadCount()))
                .isTimeStage(false)
                .isCountStage(request.getConcurrencyCount() > 0)
                .isFinish(false)
                .isMonitorFinish(false)
                .build();

        StressResult stressResult = StressResult.builder().everyData(Lists.newCopyOnWriteArrayList())
                .everyTimes(Lists.newCopyOnWriteArrayList())
                .failedCounter(new AtomicInteger())
                .totalCounter(new AtomicInteger())
                .threadCount(request.getThreadCount())
                .build();

        StressThreadPool threadPool = new StressThreadPool().createThreadPool(request.getThreadCount());

        int threadCount = request.getThreadCount();

        List<StressWorker> workers = new ArrayList<>(threadCount);

        if (request.getConcurrencyCount() <= 0) {
            for (int i = 0; i < threadCount; i++) {
                StressWorker worker = new StressWorker(request.getTasks(), context);
                workers.add(worker);
            }
        } else {
            List<List<StressTask>> workerTasks = splitListByNumber(buildTotalTask(request), threadCount);
            //构建works, works与workerTasks长度一致
            for(List<StressTask> tasks : workerTasks) {
                //每一个worker都有一个任务集合,集合大小为每个线程执行次数
                StressWorker worker = new StressWorker(tasks, context);
                workers.add(worker);
            }
        }

        // 重启一个线程用于监控压测情况
        ThreadPoolUtil.execute(() -> {
            boolean isShutdown = true;
            Long startRunTime = System.currentTimeMillis();
            //执行worker
            for(StressWorker worker : workers) {
                threadPool.execute(worker);
            }

            while (isShutdown) {
                //forever 这种情况需要手动结束
                if (request.getTotalConcurrencyTime() <= 0L && request.getConcurrencyCount() <= 0) {
                    log.info("forever");
                    break;
                }

                // 总运行次数 如果先达到次数限制,退出
                if (request.getConcurrencyCount() > 0) {
                    if (stressResult.getTotalCounter().get() >= request.getThreadCount() * request.getConcurrencyCount()) {
                        log.info("count done");
                        break;
                    }
                }

                // 总运行时间 如果先达到时间限制,退出
                if (request.getTotalConcurrencyTime() > 0L) {
                    if (System.currentTimeMillis() - startRunTime >= request.getTotalConcurrencyTime()) {
                        context.setTimeStage(true);
                        log.info("time done");
                        break;
                    }
                }
            }

            try {
                context.getEndLatch().await();
                context.setFinish(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPool.shutdown();
        });

        /** 异步开启数据监控**/
        ThreadPoolUtil.execute(new AsyncStressResultHandler(request, stressResult, this.context, workers));

        return stressResult;
    }

    private static List<StressTask> buildTotalTask(StressRequest request) {
        int totalTaskCount = request.getThreadCount() * request.getConcurrencyCount();
        List<StressTask> totalTasks = new ArrayList<>(totalTaskCount);

        Iterator tasksIterator = request.getTasks().iterator();
        int taskCounter = 0;

        //构建一个总需要运行的totalTask集合
        for (int totalTaskCounter = 0; totalTaskCounter < totalTaskCount; totalTaskCounter++) {
            if (taskCounter <= totalTaskCount) {
                //重置迭代器
                if (!tasksIterator.hasNext()) {
                    tasksIterator = request.getTasks().iterator();
                }
                totalTasks.add((StressTask) tasksIterator.next());
                taskCounter++;
            }
        }
        return totalTasks;
    }

    /** list切片 **/
    private static <T> List<List<T>> splitListByNumber(List<T> source, int number) {
        List<List<T>> result = new ArrayList<>();
        //(先计算出余数)
        int remainder = source.size() % number;
        //然后是商
        int quotient = source.size() / number;
        //偏移量
        int offset = 0;
        for (int i = 0; i < number; i++) {
            List<T> value;
            if (remainder > 0) {
                value = source.subList(i * quotient + offset, (i + 1) * quotient + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * quotient + offset, (i + 1) * quotient + offset);
            }
            result.add(value);
        }
        return result;
    }

    public static void main(String[] args) throws InterruptedException {
        List<StressTask<String>> tasks = Lists.newArrayList(new LogTask("1"), new LogTask("2"), new LogTask("3"), new LogTask("4"), new LogTask("5"), new LogTask("6"), new LogTask("7"));
        StressRequest<String> stressRequest = StressRequest.<String>builder()
                        .tasks(tasks)
                        .validate(StressRequest.Validate.builder()
                                        .rule(ValidateRule.EQUALS)
                                        .target(ValidateTarget.RESPONSE_VALUE)
                                        .data("2").build())
                        .threadCount(10)
                        .totalConcurrencyTime(10L * 1000)
                        .build();
        StressTester tester = new StressTester();
        StressResult stressResult = tester.test(stressRequest);

        ScheduledThreadPoolUtil.scheduleAtFixedRateByCompute(()-> StressFormat.format(stressResult),
                0,
                1,
                () -> {
                    if (tester.getContext().isMonitorFinish()){
                        ThreadPoolUtil.shutdown();
                        ScheduledThreadPoolUtil.shutdown();
                        StressFormat.format(stressResult);
                        return true;
                    }
                    return false;
                },
                TimeUnit.SECONDS);
    }
}
