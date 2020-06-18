package core;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 完成后结果保存类
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
@Data
@Builder
public class StressResult {
    /** 统计任务执行失败数量 */
    private AtomicInteger failedCounter;

    /** 执行总数 **/
    private AtomicInteger totalCounter;

    /**
     *  每一个请求的时间间隔 ms
     */
    private CopyOnWriteArrayList<Long> everyTimes;

    /**
     * 每一个请求的返回值
     */
    private CopyOnWriteArrayList<Object> everyData;
}
