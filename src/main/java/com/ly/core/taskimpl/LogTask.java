package com.ly.core.taskimpl;

import com.ly.core.StressTask;

/**
 * @Author: luoy
 * @Date: 2020/6/16 18:15.
 */
public class LogTask implements StressTask<String> {

    private String log;

    public LogTask(String log) {
        this.log = log;
    }

    @Override
    public String task() throws Throwable {
        Thread.sleep(50);
//        System.out.println(Thread.currentThread().getName() + ": " + log);
//        if (log.equals("2")) {
//            throw new RuntimeException("test error");
//        }
        return log;
    }
}
