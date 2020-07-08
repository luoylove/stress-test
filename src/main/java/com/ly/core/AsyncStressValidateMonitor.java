package com.ly.core;

import com.ly.core.util.HttpResponse;
import com.ly.core.util.ThreadPoolUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 异步断言压测结果
 * @Author: luoy
 * @Date: 2020/7/8 14:02.
 */
public class AsyncStressValidateMonitor {
    public static void validateMonitor(StressRequest request, StressContext context, StressResult result) {
        ThreadPoolUtil.execute(() -> {
            int next = 0;
            while (!context.getIsFinish()) {
                if (result.getEveryData().size() == 0) {
                    continue;
                }
                if (next == 0) {
                    next = result.getEveryData().size();
                    result.getEveryData().forEach(data -> {
                        boolean isValidate = doValidate(request.getValidate(), data);
                        if (!isValidate) {
                            result.getFailedCounter().getAndIncrement();
                        }
                    });
                } else {
                    for (int i = next; i < result.getEveryData().size(); i++) {
                        boolean isValidate = doValidate(request.getValidate(), result.getEveryData().get(i));
                        if (!isValidate) {
                            result.getFailedCounter().getAndIncrement();
                        }
                    }
                    next = result.getEveryData().size();
                }
            }
            System.out.println("validate thread return");
        });
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
}
