package core.taskimpl;

import core.util.HttpRequestJson;
import core.util.HttpResponse;
import core.util.HttpUtil;

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
        return HttpUtil.getInstance().post4Json(params);
    }
}
