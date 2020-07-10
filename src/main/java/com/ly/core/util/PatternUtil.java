package com.ly.core.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: luoyoujun
 * @Date: 2019/9/19 14:48.
 */
public class PatternUtil {
    private static Pattern TAG_PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=\\})");

    /**
     * 返回匹配${}中的字符串
     * @param s
     * @return
     */
    public static List<String> getPatterns(String s) {
        List<String> patterns = Lists.newArrayList();
        Matcher m = TAG_PATTERN.matcher(s);
        while(m.find()){
            patterns.add(m.group());
        }
        return patterns;
    }

    public static String createKey(String key) {
        return "${" + key + "}";
    }
}
