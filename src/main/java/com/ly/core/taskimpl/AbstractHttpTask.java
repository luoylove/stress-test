package com.ly.core.taskimpl;

import com.ly.core.StressTask;
import com.ly.core.util.HttpResponse;
import com.ly.core.util.HttpUtil;

/**
 * @Author: luoy
 * @Date: 2020/6/19 13:43.
 */
public abstract class AbstractHttpTask implements StressTask<HttpResponse> {
    /**
     * 子类初始化的时候（不管是通过有参构造还是无参构造），如果继承有父类，都会先去调用父类的无参构造。
     * 把httpUtil类加载到jvm中触发static块
     */
    public AbstractHttpTask() {
        HttpUtil.init();
    }

    @Override
    public HttpResponse task() throws Throwable {
        return doTask();
    }

    public abstract HttpResponse doTask() throws Throwable;
}
