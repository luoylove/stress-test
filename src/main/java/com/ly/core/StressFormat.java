package com.ly.core;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: luoy
 * @Date: 2020/6/24 10:15.
 */
@Slf4j
public class StressFormat {
    public static void format(StressResult result) {
        //所有task运行时长
        Double totalTaskTime = getTotalTimeToMs(result);
        //总次数
        int totalCount = result.getTotalCounter().get();
        //错误数
        int totalFailedCount = result.getFailedCounter().get();

        if (totalTaskTime <= 0 || totalCount<= 0) {
            return;
        }

        Double totalTime = nsToMs(result.getTotalTime());

        Double failedRate = getFailedRate(totalCount, totalFailedCount);

        Double aveTime = getAveTime(totalCount, totalTaskTime);

        //tps 四舍五入  总线程 * 总请求/ 总时间
        Double tps = getTps(result.getThreadCount(), totalCount, totalTaskTime);

        List<Long> sortResult = sort(result.getEveryTimes());

        int index = sortResult.size() - 1;
        Double minTime = nsToMs(sortResult.get(0));
        Double maxTime = nsToMs(sortResult.get(index));
        Double count_50 = nsToMs(sortResult.get(index / 2));
        Double count_60 = nsToMs(sortResult.get(index * 60 / 100));
        Double count_70 = nsToMs(sortResult.get(index * 70 / 100));
        Double count_80 = nsToMs(sortResult.get(index * 80 / 100));
        Double count_90 = nsToMs(sortResult.get(index * 90 / 100));
        Double count_95 = nsToMs(sortResult.get(index * 95 / 100));
        Double count_99 = nsToMs(sortResult.get(index * 99 / 100));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("并发数:").append(result.getThreadCount()).append("\n")
                .append("总执行次数:").append(totalCount).append("\n")
                .append("总耗时:").append(totalTime).append(" ms").append("\n")
                .append("错误数: ").append(totalFailedCount).append("\n")
                .append("错误率: ").append(failedRate).append("%").append("\n")
                .append("TPS:").append(tps).append("\n")
                .append("最小耗时:").append(minTime).append(" ms").append("\n")
                .append("最大耗时:").append(maxTime).append(" ms").append("\n")
                .append("平均耗时:").append(aveTime).append(" ms").append("\n")
                .append("50%的耗时在").append(count_50).append(" ms以内").append("\n")
                .append("60%的耗时在").append(count_60).append(" ms以内").append("\n")
                .append("70%的耗时在").append(count_70).append(" ms以内").append("\n")
                .append("80%的耗时在").append(count_80).append(" ms以内").append("\n")
                .append("90%的耗时在").append(count_90).append(" ms以内").append("\n")
                .append("95%的耗时在").append(count_95).append(" ms以内").append("\n")
                .append("99%的耗时在").append(count_99).append(" ms以内").append("\n");

        log.info("\n" + stringBuilder.toString());
    }

    private static Double getAveTime(int totalCount, Double totalTaskTime) {
        return new BigDecimal(String.valueOf(totalTaskTime))
                .divide(new BigDecimal(String.valueOf(totalCount)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getTps(int threadCount, int totalCount, Double totalTaskTime) {
        return new BigDecimal(String.valueOf(totalCount * threadCount))
                .divide(new BigDecimal(String.valueOf(totalTaskTime / 1000)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getFailedRate(int totalCount, int totalFailed) {
        return new BigDecimal(String.valueOf(totalFailed))
                .divide(new BigDecimal(String.valueOf(totalCount)), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
    }

    private static Double getTotalTimeToMs(StressResult result) {
        if (result.getEveryTimes().size() > 0) {
            long sum = result.getEveryTimes().stream().mapToLong((x) -> (long) x).sum();
            return nsToMs(sum);
        } else {
            return 0d;
        }
    }

    private static Double nsToMs(Long time) {
        if (time == null || time <= 0) {
            return 0d;
        }
        return new BigDecimal(String.valueOf(time))
                .divide(new BigDecimal(String.valueOf(1000 * 1000)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double nsToS(Double time) {
        return new BigDecimal(String.valueOf(time))
                .divide(new BigDecimal(String.valueOf(1000 * 1000 * 1000)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static List<Long> sort(List<Long> collection) {
        return collection.stream().sorted().collect(Collectors.toList());
    }
}
