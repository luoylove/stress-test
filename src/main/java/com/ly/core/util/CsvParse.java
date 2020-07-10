package com.ly.core.util;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoyoujun on 2019/6/17.
 */
public class CsvParse {
    public static Object[][] getParseFromCsv(String filePath) {
        try {
            return getParseFromCsv(filePath, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(
                    "No parameter values available for method: "+ e.getMessage() + "请检查csv文件是否存在，路径："
                            + filePath + "或格式是否正确");
        }
    }

    public static Object[][] getParseFromCsv(String filePath, String encode) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis, encode);
             CSVReader reader = new CSVReader(isr)){

            List<Object[]> result = new ArrayList<>();
            Object[] nextLine ;

            while ((nextLine = reader.readNext()) != null) {
                result.add(nextLine);
            }

            Object[][] objects = list2Array(result);
            if ((null == objects) || (objects.length == 0)) {
                throw new RuntimeException(
                        "No parameter values available for method: " + "请检查csv文件是否存在，路径：" + filePath + "或格式是否正确");
            }
            return objects;
        }
    }

    private static Object[][] list2Array(List<Object[]> result) {
        if ((null == result) || (result.isEmpty())) {
            return null;
        }
        int size = result.size();
        Object[][] objects = new Object[size][];
        for (int i = 0; i < size; i++) {
            objects[i] =  result.get(i);
        }
        return objects;
    }
}
