package core.taskimpl;

import core.util.HttpRequestParams;
import core.util.HttpResponse;
import core.util.HttpUtil;

/**
 * http post4form task
 * @Author: luoy
 * @Date: 2020/6/19 16:22.
 */
public class HttpPostFormTask extends AbstractHttpTask{

    private HttpRequestParams params;

    public HttpPostFormTask(HttpRequestParams params) {
        this.params = params;
    }

    @Override
    public HttpResponse doTask() throws Throwable {
        return HttpUtil.getInstance().post4Para(params);
    }
}
