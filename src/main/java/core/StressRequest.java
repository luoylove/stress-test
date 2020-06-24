package core;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author: luoy
 * @Date: 2020/6/11 9:53.
 */
@Data
@Builder
public class StressRequest<T> {
    /**所有需要运行的task,少于总运行数循环执行*/
    private List<StressTask<T>> tasks;

    /**
     * 总执行时长 ms
     */
    private long totalConcurrencyTime;

    /**
     * 线程数量
     */
    private int threadCount;

    /**
     * 单个线程执行次数
     */
    private int concurrencyCount;
}
