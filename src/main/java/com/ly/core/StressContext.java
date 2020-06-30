package com.ly.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * 上下文
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StressContext {

    /** 设置线程集合点栅栏**/
    private CyclicBarrier startBarrier;

    /** 设置结束哨兵,监控所有任务是否都执行完 */
    private CountDownLatch endLatch;

    /**是否达到时长运行阈值 */
    private boolean isTimeStage;

    /** 是否有运行次数限制,如果运行次数大于0,为true */
    private boolean isCountStage;
}
