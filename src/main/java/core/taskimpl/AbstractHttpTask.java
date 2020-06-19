package core.taskimpl;

import core.StressTask;
import core.util.HttpResponse;

/**
 * @Author: luoy
 * @Date: 2020/6/19 13:43.
 */
public abstract class AbstractHttpTask implements StressTask {
    @Override
    public Object task() throws Throwable {
        return doTask();
    }

    public abstract HttpResponse doTask() throws Throwable;
}
