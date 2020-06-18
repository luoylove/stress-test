import com.google.common.collect.Lists;
import core.StressContext;
import core.StressRequest;
import core.StressResult;
import core.StressTask;
import core.StressThreadPool;
import core.StressWorker;
import core.taskimpl.LogTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: luoy
 * @Date: 2020/6/16 16:06.
 */
public class StressTester {

    public static StressResult test(StressRequest request) {
        StressContext stressContext = StressContext.builder().startBarrier(new CyclicBarrier(request.getThreadCount()))
                .endLatch(new CountDownLatch(request.getThreadCount()))
                .isTimeStage(false)
                .build();

        StressResult stressResult = StressResult.builder().everyData(Lists.newCopyOnWriteArrayList())
                .everyTimes(Lists.newCopyOnWriteArrayList())
                .failedCounter(new AtomicInteger())
                .totalCounter(new AtomicInteger())
                .build();

        StressThreadPool threadPool = new StressThreadPool().createThreadPool(request.getThreadCount());

        int threadCount = request.getThreadCount();

        List<StressWorker> workers = new ArrayList<>(threadCount);

        List<List<StressTask>> workerTasks = splitListByNumber(buildTotalTask(request), threadCount);

        //构建works, works与workerTasks长度一致
        for(List<StressTask> tasks : workerTasks) {
            //每一个worker都有一个任务集合,集合大小为每个线程执行次数
            StressWorker worker = new StressWorker(tasks, stressContext, stressResult);
            workers.add(worker);
        }

        for(StressWorker worker : workers) {
            threadPool.execute(worker);
        }

        boolean isShutDownByTime = true;
        Long startRunTime = System.currentTimeMillis();

        while (isShutDownByTime) {
            if (request.getTotalConcurrencyTime() == null || request.getTotalConcurrencyTime() == 0L) {
                break;
            }

            if (stressResult.getTotalCounter().get() >= request.getThreadCount() * request.getConcurrencyCount()) {
                break;
            }

            if (System.currentTimeMillis() - startRunTime >= request.getTotalConcurrencyTime()) {
                stressContext.setTimeStage(true);
                break;
            }
        }

        try {
            stressContext.getEndLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadPool.shutdown();
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

    public static void main(String[] args) {
        List<StressTask> tasks = Lists.newArrayList(new LogTask("1"), new LogTask("2"), new LogTask("3"), new LogTask("4"), new LogTask("5"), new LogTask("6"), new LogTask("7"));
        StressRequest stressRequest = StressRequest.builder().tasks(tasks).threadCount(10).concurrencyCount(50).totalConcurrencyTime(8L * 1000).build();
        StressResult stressResult = test(stressRequest);
        System.out.println(stressResult.toString());
    }
}
