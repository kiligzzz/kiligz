package com.kiligz.concurrent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * future任务的处理
 * 支持CompletableFuture
 * 支持监控
 *
 * @author ivan.zhu
 * @since 2024/7/3
 */
@Data
@Slf4j
@SuppressWarnings("all")
public class FutureTasks<T> {
    private static final String DEFAULT_NAME = "futureTasks";
    private static final int DEFAULT_PERIOD = 10;
    private static final int DEFAULT_BATCH = 1000;

    private final int total;
    private final AtomicInteger finished;
    private final Queue<T> resQueue;
    // 限制并发数
    private final Semaphore semaphore;
    // 等待任务执行完成
    private final CountDownLatch countDownLatch;
    private final ScheduledExecutorService scheduler;
    private final String name;
    // 异常处理
    private final AtomicReference<Throwable> eRef = new AtomicReference<>();


    public FutureTasks(int total) {
        this(total, DEFAULT_NAME, DEFAULT_PERIOD);
    }

    public FutureTasks(int total, String name) {
        this(total, DEFAULT_BATCH, name, DEFAULT_PERIOD);
    }

    public FutureTasks(int total, String name, int period) {
        this(total, DEFAULT_BATCH, name, period);
    }

    /**
     * 输入总数和批处理个数，带监控的futureTasks，打印间隔，单位：s
     */
    public FutureTasks(int total, int batch, String name, int period) {
        this.total = total;
        this.finished = new AtomicInteger();
        this.resQueue = new LinkedBlockingQueue<>();
        this.semaphore = new Semaphore(batch);
        this.countDownLatch = new CountDownLatch(total);
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        this.name = name;
        this.scheduler.scheduleAtFixedRate(this::status, 0, period, TimeUnit.SECONDS);
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
                finished.getAndIncrement();
                semaphore.release();
                countDownLatch.countDown();
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
                    scheduler.shutdown();
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
            scheduler.shutdown();
            log.error("===> [{}] exec error. ", name, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 监控信息
     */
    private void status() {
        log.info("===> [{}] total tasks: {}, has finish: {}", name, total, finished.get());
    }

    /**
     * future任务的处理
     * 阉割版，支持Future
     */
    public static class LimitedFutureTasks<T> {
        List<Future<T>> futureList = new ArrayList<>();

        public void add(Future<T> future) {
            futureList.add(future);
        }

        public void awaitAll() {
            getAll();
        }

        public void awaitAny() {
            getAny();
        }

        public List<T> getAll() {
            try {
                List<T> res = new ArrayList<>();
                for (Future<T> future : futureList) {
                    res.add(future.get());
                }
                return res;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public T getAny() {
            try {
                return futureList.get(ThreadLocalRandom.current().nextInt(futureList.size())).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
