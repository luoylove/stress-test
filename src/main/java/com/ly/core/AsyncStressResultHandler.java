package com.ly.core;

import com.google.common.collect.Lists;
import com.ly.core.util.HttpResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 异步断言压测结果
 * @Author: luoy
 * @Date: 2020/7/8 14:02.
 */
public class AsyncStressResultHandler implements Runnable{

    /** 监控间隔 */
    private static final Long MONITOR_TIME_INTERVAL = 100L;

    private StressRequest request;

    private StressResult result;

    private StressContext context;

    private List<StressWorker> workers;

    public AsyncStressResultHandler(StressRequest request, StressResult result, StressContext context, List<StressWorker> workers) {
        this.request = request;
        this.result = result;
        this.context = context;
        this.workers = workers;
    }

    public void handler() {
        List<Object> everyData = Lists.newArrayList();
        List<Object> everyTime = Lists.newArrayList();
        List<Long> threadTime = Lists.newArrayListWithCapacity(request.getThreadCount());

        int next = 0;

        while (!context.isMonitorFinish()) {
            int totalCount = 0;
            for (int workerCounter = 0; workerCounter < workers.size(); workerCounter++) {
                StressWorker worker = workers.get(workerCounter);
                if (threadTime.size() <= request.getThreadCount()) {
                    threadTime.add(worker.getLocalTimeSum());
                } else {
                    threadTime.set(workerCounter, threadTime.get(workerCounter) + worker.getLocalTimeSum());
                }

                totalCount += worker.getLocalTotalCount();

                //native copy
                everyTime.addAll(worker.getLocalTime());
                everyData.addAll(worker.getLocalData());
                worker.getLocalData().clear();
                worker.getLocalTime().clear();
            }

            if(threadTime.size() > 0) {
                result.setTotalTime(threadTime.stream().max(Long::compare).get());
            }

            result.getTotalCounter().set(totalCount);

            //每次清除后再赋值
            result.getEveryTimes().clear();
            result.getEveryData().clear();
            result.getEveryTimes().addAll(everyTime);
            result.getEveryData().addAll(everyData);

            int size = everyData.size();
            if (size > 0 && next != size) {
                for (int i = next; i < size; i++) {
                    if (result.getEveryData().get(i) instanceof Throwable) {
                        result.getFailedCounter().getAndIncrement();
                    } else {
                        boolean isValidate = doValidate(request.getValidate(), result.getEveryData().get(i));
                        if (!isValidate) {
                            result.getFailedCounter().getAndIncrement();
                        }
                    }
                }
                next = size;
            }
            if (context.isFinish() && next == size) {
                context.setMonitorFinish(true);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(MONITOR_TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean doValidate(StressRequest.Validate validate, Object res) {
        if (validate == null) {
            return true;
        }

        switch (validate.getTarget()) {
            case RESPONSE_CODE:
                if (res instanceof HttpResponse) {
                    HttpResponse httpResponse = (HttpResponse) res;
                    return validateRule(validate, String.valueOf(httpResponse.getStatusCode()));
                }
            case RESPONSE_VALUE:
                if (res instanceof HttpResponse) {
                    HttpResponse httpResponse = (HttpResponse) res;
                    return validateRule(validate, httpResponse.getBody());
                } else {
                    return validateRule(validate, String.valueOf(res));
                }
            default:
                return false;
        }
    }

    private static boolean validateRule(StressRequest.Validate validate, String res) {
        if(validate == null || StringUtils.isBlank(res)) {
            return true;
        }
        switch (validate.getRule()) {
            case REGEX:
                Pattern pattern = Pattern.compile(String.valueOf(validate.getData()));
                Matcher matcher = pattern.matcher(res);
                return matcher.matches();
            case EQUALS:
                return String.valueOf(validate.getData()).equals(res);
            case CONTAINS:
                return res.contains(String.valueOf(validate.getData()));
            case NOT_EQUALS:
                return !String.valueOf(validate.getData()).equals(res);
            case NOT_CONTAIN:
                return !res.contains(String.valueOf(validate.getData()));
            default:
                return false;
        }
    }

    @Override
    public void run() {
        handler();
    }
}
