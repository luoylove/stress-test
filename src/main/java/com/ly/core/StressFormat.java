package com.ly.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: luoy
 * @Date: 2020/6/24 10:15.
 */
public class StressFormat {
    public static void format(StressResult result) {
        //所有task运行时长
        Double totalTaskTime = getTotalTimeToMs(result);
        //总次数
        int totalCount = result.getTotalCounter().get();
        //错误数
        int totalFailedCount = result.getFailedCounter().get();

        if (totalTaskTime <= 0 && totalCount<= 0) {
            return;
        }

        //物理实际耗时
        Double totalPhysicsTime = getTotalPhysicsTime(totalTaskTime, result.getThreadCount());

        Double failedRate = getFailedRate(totalCount, totalFailedCount) * 100;

        Double aveTime = getAveTime(totalCount, totalTaskTime);

        //tps 四舍五入
        Double tps = getTps(totalCount, totalPhysicsTime);

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
                .append("总耗时:").append(totalPhysicsTime).append(" ms").append("\n")
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

        System.out.println(stringBuilder.toString());
    }

    private static Double getTotalPhysicsTime(Double totalTaskTime, int threadCount) {
        return new BigDecimal(String.valueOf(totalTaskTime))
                .divide(new BigDecimal(String.valueOf(threadCount)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getAveTime(int totalCount, Double totalTaskTime) {
        return new BigDecimal(String.valueOf(totalTaskTime))
                .divide(new BigDecimal(String.valueOf(totalCount)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getTps(int totalCount, Double totalPhysicsTime) {
        return new BigDecimal(String.valueOf(totalCount))
                .divide(new BigDecimal(String.valueOf(totalPhysicsTime / 1000)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getFailedRate(int totalCount, int totalFailed) {
        if (totalFailed <= 0) {
            return 0.00;
        }
        return new BigDecimal(String.valueOf(totalFailed))
                .divide(new BigDecimal(String.valueOf(totalCount)), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    private static Double getTotalTimeToMs(StressResult result) {
        long sum = result.getEveryTimes().stream().mapToLong((x) -> (long) x).sum();
        return nsToMs(sum);
    }

    private static Double nsToMs(Long time) {
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
