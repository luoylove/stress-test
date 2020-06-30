package com.ly.core.taskimpl;

import com.ly.core.StressTask;

/**
 * @Author: luoy
 * @Date: 2020/6/19 13:43.
 */
public abstract class AbstractHttpTask<HttpResponse> implements StressTask {
    @Override
    public HttpResponse task() throws Throwable {
        return doTask();
    }

    public abstract HttpResponse doTask() throws Throwable;
}
