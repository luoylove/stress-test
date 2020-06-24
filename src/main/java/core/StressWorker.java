package core;

import java.util.List;

/**
 * 线程主体
 *
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
public class StressWorker<T> implements Runnable{
    private List<StressTask> stressTasks;

    private StressContext stressContext;

    private StressResult stressResult;

    public StressWorker(List<StressTask> stressTasks, StressContext stressContext, StressResult stressResult) {
        this.stressTasks = stressTasks;
        this.stressContext = stressContext;
        this.stressResult = stressResult;
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
                    throwable.printStackTrace();
                    //如果执行失败记录时间点并且失败+1
                    endTime = System.nanoTime();
                    stressResult.getFailedCounter().getAndIncrement();
                    isFailed = true;
                } finally {
                    Long everyTime;
                    if (!isFailed) {
                        everyTime = System.nanoTime() - startTime;
                    } else {
                        everyTime = endTime - startTime;
                    }
                    stressResult.getTotalCounter().getAndIncrement();
                    stressResult.getEveryData().add(res);
                    stressResult.getEveryTimes().add(everyTime);
                }
            }
            if (stressContext.isCountStage()) {
                return;
            }
        }
    }
}
