package com.kiligz.retry;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 重试工具类
 *
 * @author ivan.zhu
 * @since 2024/7/8
 */
@Slf4j
public class RetryUtil {
    /**
     * 出现异常重试，默认10次，立即重试
     */
    public static <T> T retry(Callable<T> callable) {
        return retry(10, 0, callable);
    }

    /**
     * 出现异常重试
     * 重试次数
     * 重试间隔，单位s
     */
    public static <T> T retry(int times, int interval, Callable<T> callable) {
        try {
            return RetryerBuilder.<T>newBuilder()
                    .retryIfException()
                    .withStopStrategy(StopStrategies.stopAfterAttempt(times))
                    .withWaitStrategy(WaitStrategies.fixedWait(interval, TimeUnit.SECONDS))
                    .build()
                    .call(callable);
        } catch (ExecutionException | RetryException e) {
            log.error("重试操作失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据结果重试，默认10次，立即重试
     */
    public static <T> T retry(Callable<T> callable, Predicate<T> predicate) {
        return retry(10, 0, callable, predicate);
    }

    /**
     * 根据结果重试
     * 重试次数
     * 重试间隔，单位s
     */
    public static <T> T retry(int times, int interval, Callable<T> callable, Predicate<T> predicate) {
        try {
            return RetryerBuilder.<T>newBuilder()
                    .retryIfResult(predicate)
                    .withStopStrategy(StopStrategies.stopAfterAttempt(times))
                    .withWaitStrategy(WaitStrategies.fixedWait(interval, TimeUnit.SECONDS))
                    .build()
                    .call(callable);
        } catch (ExecutionException | RetryException e) {
            log.error("重试操作失败", e);
            throw new RuntimeException(e);
        }
    }
}
