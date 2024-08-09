package com.kiligz.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 可重入的自旋锁，cas实现
 *
 * @author ivan.zhu
 * @since 2024/7/12
 */
public class ReentrantSpinLock {

    private final AtomicReference<Thread> lock = new AtomicReference<>();

    private int lockCount;

    /**
     * 加锁
     */
    public void lock() {
        while (!tryLock()) {
            LockSupport.parkNanos(1);
        }
    }

    /**
     * 尝试一次加锁
     */
    public boolean tryLock() {
        Thread currentThread = Thread.currentThread();
        if (currentThread == lock.get()) {
            lockCount++;
            return true;
        } else {
            if (lock.compareAndSet(null, currentThread)) {
                lockCount = 1;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 解锁
     */
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != lock.get()) {
            throw new IllegalMonitorStateException("Calling thread has not locked this lock");
        }
        lockCount--;
        if (lockCount == 0) {
            lock.set(null);
        }
    }
}
