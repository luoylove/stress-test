package core.taskimpl;

import core.util.HttpRequestParams;
import core.util.HttpResponse;
import core.util.HttpUtil;

/**
 * http get task
 * @Author: luoy
 * @Date: 2020/6/19 16:22.
 */
public class HttpGetTask extends AbstractHttpTask{

    private HttpRequestParams params;

    public HttpGetTask(HttpRequestParams params) {
        this.params = params;
    }

    @Override
    public HttpResponse doTask() throws Throwable {
        return HttpUtil.getInstance().get(params);
    }
}
