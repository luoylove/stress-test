package com.ly.core.util;

/**
 * 用于 scheduledPool中计算是否退出定时任务
 * @Author: luoy
 * @Date: 2020/8/10 17:11.
 */
@FunctionalInterface
public interface Computable {
    public abstract boolean compute();
}
