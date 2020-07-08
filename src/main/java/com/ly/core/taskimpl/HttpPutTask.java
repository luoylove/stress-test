package com.ly.core.taskimpl;

import com.ly.core.util.HttpRequestParams;
import com.ly.core.util.HttpResponse;
import com.ly.core.util.HttpUtil;

/**
 * http put task
 * @Author: luoy
 * @Date: 2020/6/19 16:22.
 */
public class HttpPutTask extends AbstractHttpTask{

    private HttpRequestParams params;

    public HttpPutTask(HttpRequestParams params) {
        this.params = params;
    }

    @Override
    public HttpResponse doTask() throws Throwable {
        return HttpUtil.put(params);
    }
}
