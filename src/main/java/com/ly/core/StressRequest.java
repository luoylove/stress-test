package com.ly.core;

import com.ly.core.enums.ValidateRule;
import com.ly.core.enums.ValidateTarget;
import com.ly.core.tcp.message.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: luoy
 * @Date: 2020/6/11 9:53.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StressRequest<T> implements Message {
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

    /**
     * 断言
     */
    private Validate validate;

    /**
     * 验证模式
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Validate{
        /** 匹配规则 */
        private ValidateRule rule;

        /** 断言对象 */
        private ValidateTarget target;

        /**断言值*/
        private Object data;
    }
}
