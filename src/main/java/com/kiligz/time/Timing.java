package com.kiligz.time;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 计时工具类，支持线程内计时
 * 
 * @author Ivan
 * @since 2023/3/8
 */
public class Timing {
    /**
     * 线程本地变量
     */
    private static final ThreadLocal<Deque<Instant>> THREAD_LOCAL_START = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 开始计时
     */
    public static void start() {
        Instant start = Instant.now();
        THREAD_LOCAL_START.get().addFirst(start);
    }

    /**
     * 结束并返回耗时，默认单位：s
     */
    public static long end() {
        return endDuration().getSeconds();
    }

    /**
     * 结束并返回耗时
     */
    public static Duration endDuration() {
        Instant end = Instant.now();
        Instant start = THREAD_LOCAL_START.get().pollFirst();
        if (start == null)
            return Duration.ofSeconds(-1);
        return Duration.between(start, end);
    }

    /**
     * 结束并打印耗时
     */
    public static void endWithLog() {
        System.out.println(end());
    }
}