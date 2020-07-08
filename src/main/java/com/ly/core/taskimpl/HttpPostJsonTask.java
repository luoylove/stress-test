package com.ly.core.taskimpl;

import com.ly.core.util.HttpRequestJson;
import com.ly.core.util.HttpResponse;
import com.ly.core.util.HttpUtil;

/**
 * http post4json task
 * @Author: luoy
 * @Date: 2020/6/19 16:22.
 */
public class HttpPostJsonTask extends AbstractHttpTask{

    public HttpPostJsonTask(HttpRequestJson params) {
        this.params = params;
    }

    private HttpRequestJson params;

    @Override
    public HttpResponse doTask() throws Throwable {
        return HttpUtil.post4Json(params);
    }
}
