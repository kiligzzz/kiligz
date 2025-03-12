package com.kiligz.concurrent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * future任务的处理
 * 支持CompletableFuture
 * 支持控制任务执行速度，避免一下占用太多内存
 * 支持监控
 *
 * @author ivan.zhu
 * @since 2024/7/3
 */
@Slf4j
@Getter
@SuppressWarnings("all")
public class FutureTasks<T> {
    private static final String DEFAULT_NAME = "futureTasks";
    private static final int DEFAULT_PERIOD = 10;
    private static final int DEFAULT_BATCH = 1000;

    /**
     * 任务总数
     */
    private final int total;
    /**
     * 结果队列
     */
    private final Queue<T> resQueue;
    /**
     * 等待任务执行完成
     */
    private final CountDownLatch countDownLatch;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 限制并发数
     */
    private Semaphore semaphore;
    /**
     * 单任务完成打印执行状态和返回结果
     */
    private boolean detailStatus;
    /**
     * 默认定时打印执行状态
     */
    private ScheduledExecutorService scheduler;
    /**
     * 异常处理
     */
    private final AtomicReference<Throwable> eRef = new AtomicReference<>();

    public FutureTasks(int total) {
        this(total, DEFAULT_NAME);
    }

    public FutureTasks(int total, String name) {
        this(total, name, DEFAULT_BATCH, DEFAULT_PERIOD, false);
    }

    /**
     * 输入总数和批处理个数，带监控的futureTasks，可打印详细信息或定时打印，单位：s
     */
    public FutureTasks(int total, String name, int batch, int period, boolean detailStatus) {
        this.total = total;
        this.resQueue = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(batch);
        this.countDownLatch = new CountDownLatch(total);
        this.name = name;
        this.detailStatus = detailStatus;
        if (!detailStatus) {
            this.scheduler = new ScheduledThreadPoolExecutor(1);
            this.scheduler.scheduleAtFixedRate(this::status, 0, period, TimeUnit.SECONDS);
        }
    }

    /**
     * 设置批处理个数
     */
    public void setBatch(int batch) {
        semaphore = new Semaphore(batch);
    }

    /**
     * 设置打印日志周期
     */
    public void setPeriod(int period) {
        shutdownScheduler();
        scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.scheduleAtFixedRate(this::status, 0, period, TimeUnit.SECONDS);
    }

    /**
     * 开启详细输出
     */
    public void openDetail() {
        shutdownScheduler();
        detailStatus = true;
    }

    /**
     * 任务future结果
     */
    public void add(CompletableFuture<T> cf) {
        try {
            semaphore.acquire();
            // 完成后收集结果、更新监控状态并释放并发许可
            cf.whenComplete((res, e) -> {
                if (res != null) {
                    resQueue.add(res);
                }
                if (e != null) {
                    eRef.set(e);
                }
                semaphore.release();
                countDownLatch.countDown();

                if (detailStatus) {
                    status(res);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 等待所有任务执行完成
     */
    public void awaitAll() {
        get(false, queue -> null);
    }

    /**
     * 等待任一任务执行完成
     */
    public void awaitAny() {
        get(true, queue -> null);
    }

    /**
     * 获取所有结果
     */
    public List<T> getAll() {
        return (List<T>) get(false, ArrayList::new);
    }

    /**
     * 获取任一任务结果
     */
    public T getAny() {
        return (T) get(true, Queue::poll);
    }

    /**
     * 获取任务结果
     */
    public Object get(boolean isAny, Function<Queue<T>, Object> resFunction) {
        try {
            int period = isAny ? DEFAULT_PERIOD / 10 : DEFAULT_PERIOD;
            while (true) {
                boolean await = countDownLatch.await(period, TimeUnit.SECONDS);
                checkException();
                if (await || (isAny && countDownLatch.getCount() < total)) {
                    status();
                    shutdownScheduler();
                    return resFunction.apply(resQueue);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查是否存在异常
     */
    private void checkException() {
        Throwable e = eRef.get();
        if (e != null) {
            shutdownScheduler();
            log.error("===> [{}] exec error. ", name, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 监控信息
     */
    private void status() {
        log.info("===> [{}] finish tasks: {}/{}.", name, total - countDownLatch.getCount(), total);
    }

    /**
     * 详细监控信息
     */
    private void status(T res) {
        log.info("===> [{}] finish tasks: {}/{}. res: {}", name, total - countDownLatch.getCount(), total, res);
    }

    /**
     * 关闭定时器
     */
    private void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
