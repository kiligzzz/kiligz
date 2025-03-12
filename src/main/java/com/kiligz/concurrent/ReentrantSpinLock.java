package com.kiligz.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 可重入的自旋锁，CAS实现
 *
 * @author ivan.zhu
 * @since 2024/7/12
 */
public class ReentrantSpinLock {
//    @jdk.internal.vm.annotation.Contended 解决缓存伪共享问题
    private final AtomicReference<Thread> owner = new AtomicReference<>();
    private final AtomicInteger count = new AtomicInteger(0);
    private static final int BASE_DELAY_NS = 100;    // 基础等待时间 100ns
    private static final int MAX_DELAY_NS = 100_000; // 最大等待时间 100μs
    private static final int MAX_SPINS = Runtime.getRuntime().availableProcessors() * 10;

    /**
     * 加锁
     */
    public void lock() {
        int spins = 0;
        long delay = BASE_DELAY_NS;
        while (!tryLock()) {
            // 阶段1：纯自旋
            if (spins < MAX_SPINS) {
                Thread.onSpinWait();
                spins++;
            }
            // 阶段2：指数退避
            else {
                LockSupport.parkNanos(delay);
                delay = Math.min(delay * 2, MAX_DELAY_NS);
            }
        }
    }

    /**
     * 尝试一次加锁
     */
    public boolean tryLock() {
        Thread current = Thread.currentThread();
        if (current == owner.get()) {
            count.incrementAndGet();
            return true;
        } else if (owner.compareAndSet(null, current)) {
            count.set(1);
            return true;
        }
        return false;
    }

    /**
     * 解锁
     */
    public void unlock() {
        Thread current = Thread.currentThread();
        if (current != owner.get()) {
            throw new IllegalMonitorStateException("not lock owner");
        }
        if (count.decrementAndGet() == 0) {
            owner.set(null); // 此处无需 CAS，只有持有者线程能调用
        }
    }
}
