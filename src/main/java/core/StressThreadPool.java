package core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具
 * 执行顺序：corePoolSize -> 任务队列 -> maximumPoolSize -> 拒绝策略
 * 压测线程池直接设置活跃线程数为最大线程数,且任务队列为无界队列
 * 设置corePoolSize = maximumPoolSize, 任务队列长度为无界队列
 */
public class StressThreadPool {

    private  ThreadPoolExecutor threadPool;

    /**
     * 无返回值直接执行
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * 返回值直接执行，获取返回值 Future.get()
     *
     * @param callable
     */
    public  <T> Future<T> submit(Callable<T> callable) {
        return threadPool.submit(callable);
    }

    /**
     * 多个task提交
     * @param callables
     * @param <T>
     * @return
     */
    public  <T> List<Future<T>> submitAll(Collection<? extends Callable<T>> callables){
        try {
            return threadPool.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  StressThreadPool createThreadPool(int corePoolSize) {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }

        /**
         * 线程数的上限
         */
        int maximumPoolSize = corePoolSize;

        /**
         * 超过corePoolSize的线程的idle时长，
         * 超过这个时间，多余的线程会被回收
         * 单位unit
         */
        final long keepAliveTime = 0L;

        /**
         * 任务的排队队列
         * ArrayBlockingQueue
         * 有界队列
         * 先进先出队列（队列头的是最先进队的元素；队列尾的是最后进队的元素）
         * 有界队列（即初始化时指定的容量，就是队列最大的容量，不会出现扩容，容量满，则阻塞进队操作；容量空，则阻塞出队操作）
         * 队列不支持空元素
         * <p>
         * LinkedBlockingQueue
         * 如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列
         * <p>
         * PriorityBlockingQueue
         * 是一个基于数组实现的线程安全的无界队列
         * <p>
         * SynchronizedQueue
         * 无界的FIFO同步队列
         */
        final BlockingQueue workQueue = new LinkedBlockingQueue<>();

        /**
         * 拒绝策略
         * AbortPolicy	抛出RejectedExecutionException
         * DiscardPolicy	什么也不做，直接忽略
         * DiscardOldestPolicy	丢弃执行队列中最老的任务，尝试为当前提交的任务腾出位置
         * CallerRunsPolicy	直接由提交任务者执行这个任务
         */
        final RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();

        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                workQueue, handler);

        return this;
    }

    /**
     * 平滑关闭线程池
     */
    public  void shutdown() {
        if(threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }

    /**
     * 强制关闭线程池
     */
    public  void shutdownNow() {
        if(threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
    }


    /**
     * 已关闭 return true
     * @return
     */
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    /**
     * 关闭后所有任务是否都已完成  return true
     * while (!isTerminated())
     */
    public boolean isTerminated(){
        return threadPool.isTerminated();
    }
}