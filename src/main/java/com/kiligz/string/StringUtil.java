package com.kiligz.string;

import java.lang.reflect.Array;
import java.util.StringJoiner;

/**
 * 字符串工具类
 *
 * @author Ivan
 * @since 2023/6/30
 */
public class StringUtil {
    /**
     * 将各种数组类型转为String，包含多维
     */
    public static String arrayToString(Object obj) {
        if (obj.getClass().isArray()) {
            StringJoiner res = new StringJoiner(", ", "[", "]");
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                res.add(arrayToString(Array.get(obj, i)));
            }
            return res.toString();
        } else {
            return obj.toString();
        }
    }
}
