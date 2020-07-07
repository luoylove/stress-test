package com.ly.core;

import com.ly.core.util.HttpResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 线程主体
 *
 * @Author: luoy
 * @Date: 2020/6/9 13:36.
 */
public class StressWorker<T> implements Runnable{
    private StressRequest<T> stressRequest;

    private StressContext stressContext;

    private StressResult stressResult;

    public StressWorker(StressRequest<T> stressRequest, StressContext stressContext, StressResult stressResult) {
        this.stressRequest = stressRequest;
        this.stressContext = stressContext;
        this.stressResult = stressResult;
    }

    @Override
    public void run() {
        try {
            //设置集合点栅栏
            stressContext.getStartBarrier().await();
            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //线程执行完成后哨兵计数器-1
            stressContext.getEndLatch().countDown();
        }
    }


    private void doRun() {
        while (true) {
            for(StressTask<T> stressTask : stressRequest.getTasks()) {
                if(stressContext.isTimeStage()) {
                    return;
                }
                Long endTime = null;
                T res = null;
                Boolean isFailed = false;
                Long startTime = System.nanoTime() ;
                try {
                    res =  stressTask.task();
                } catch (Throwable throwable) {
                    endTime = System.nanoTime();
                    throwable.printStackTrace();
                    //如果执行失败记录时间点并且失败+1
                    stressResult.getFailedCounter().getAndIncrement();
                    isFailed = true;
                } finally {
                    Long everyTime;
                    if (!isFailed) {
                        everyTime = System.nanoTime() - startTime;
                        //断言
                        boolean isValidate = doValidate(stressRequest.getValidate(), res);
                        if (!isValidate) {
                            stressResult.getFailedCounter().getAndIncrement();
                        }
                    } else {
                        everyTime = endTime - startTime;
                    }
                    stressResult.getTotalCounter().getAndIncrement();
                    stressResult.getEveryData().add(res);
                    stressResult.getEveryTimes().add(everyTime);
                }
            }
            if (stressContext.isCountStage()) {
                return;
            }
        }
    }

    private boolean doValidate(StressRequest.Validate validate, Object res) {
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
            case CONTAIN:
                return res.contains(String.valueOf(validate.getData()));
            case NOT_EQUALS:
                return !String.valueOf(validate.getData()).equals(res);
            case NOT_CONTAIN:
                return !res.contains(String.valueOf(validate.getData()));
            default:
                return false;
        }
    }
}
