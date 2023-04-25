package com.kiligz.time;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 计时工具类，支持线程内计时
 * 
 * @author Ivan
 * @since 2023/3/8
 */
public abstract class Timing {
    /**
     * 线程id -> 开始时间 的map
     */
    private static final Map<Long, Instant> idToStartMap = new HashMap<>();

    /**
     * 开始计时
     */
    public static void start() {
        Instant start = Instant.now();
        idToStartMap.put(Thread.currentThread().getId(), start);
    }

    /**
     * 结束并返回耗时
     */
    public static Duration end() {
        Instant end = Instant.now();
        Instant start = idToStartMap.remove(Thread.currentThread().getId());
        return Duration.between(start, end);
    }

    /**
     * 结束并打印耗时
     */
    public static void endWithLog() {
        System.out.println(end());
    }
}