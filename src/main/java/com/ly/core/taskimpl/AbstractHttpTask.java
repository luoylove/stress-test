package com.ly.core.taskimpl;

import com.ly.core.StressTask;
import com.ly.core.util.HttpResponse;

/**
 * @Author: luoy
 * @Date: 2020/6/19 13:43.
 */
public abstract class AbstractHttpTask implements StressTask<HttpResponse> {
    @Override
    public HttpResponse task() throws Throwable {
        return doTask();
    }

    public abstract HttpResponse doTask() throws Throwable;
}
