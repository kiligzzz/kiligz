package concurrent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存安全的LinkedBlockingQueue
 * @see <a href="https://github.com/apache/shenyu/tree/master/shenyu-common">参考</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemorySafeLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    /**
     * 默认最大空闲内存
     */
    private static final int THE_256_MB = 256 * 1024 * 1024;

    /**
     * 需保证的JVM最大空闲内存
     */
    private long maxFreeMemory;

    public MemorySafeLinkedBlockingQueue() {
        this(THE_256_MB);
    }

    public MemorySafeLinkedBlockingQueue(int maxFreeMemory) {
        super();
        this.maxFreeMemory = maxFreeMemory;
    }

    public MemorySafeLinkedBlockingQueue(Collection<? extends E> c, int maxFreeMemory) {
        super(c);
        this.maxFreeMemory = maxFreeMemory;
    }

    /**
     * 是否还有剩余的内存
     */
    public boolean hasRemainedMemory() {
        return Memory.maxAvailable() > maxFreeMemory;
    }

    /**
     * put
     */
    @Override
    public void put(E e) throws InterruptedException {
        if (hasRemainedMemory()) {
            super.put(e);
        }
    }

    /**
     * offer
     */
    @Override
    public boolean offer(@NonNull E e) {
        return hasRemainedMemory() && super.offer(e);
    }

    /**
     * offer
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return hasRemainedMemory() && super.offer(e, timeout, unit);
    }

    /**
     * 可用内存的计算
     */
    public static class Memory {
        /**
         * 最大可用内存
         */
        private static volatile long maxAvailable;

        /**
         * 单线程的定时线程池
         */
        private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

        static { ;
            // 加载时立即刷新，然后每50ms检查一次
            SCHEDULER.scheduleWithFixedDelay(Memory::refresh, 0, 50, TimeUnit.MILLISECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(SCHEDULER::shutdown));
        }

        /**
         * 刷新最大可用内存
         */
        private static void refresh() {
            maxAvailable = Runtime.getRuntime().freeMemory();
        }

        /**
         * 获取最大可用内存
         */
        public static long maxAvailable() {
            return maxAvailable;
        }

        /**
         * 取最大可用的百分比
         */
        public static long calc(float percent) {
            if (percent <= 0 || percent > 1) {
                throw new IllegalArgumentException();
            }
            return (long) (maxAvailable * percent);
        }

        /**
         * 默认的可用内存（最大可用的80%）
         */
        public static long defaultLimit() {
            return (long) (maxAvailable * 0.8);
        }
    }
}