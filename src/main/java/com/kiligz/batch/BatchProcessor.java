package com.kiligz.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 线程安全的批处理器
 *
 * @author ivan.zhu
 * @since 2024/7/10
 */
public class BatchProcessor<T> {
    private final LinkedBlockingQueue<T> queue;

    private final Consumer<List<T>> batchConsumer;

    private final AtomicInteger processedSize = new AtomicInteger();

    public BatchProcessor(int batchSize, Consumer<List<T>> batchConsumer) {
        this.queue = new LinkedBlockingQueue<>(batchSize);
        this.batchConsumer = batchConsumer;
    }

    /**
     * 添加元素，当到batchSize时，由调用该方法的线程进行批处理
     */
    public void add(T t) {
        if (!queue.offer(t)) {
            List<T> list = null;
            synchronized (this) {
                if (!queue.offer(t)) {
                    list = new ArrayList<>(queue);
                    queue.clear();
                }
            }
            if (list != null) {
                batchConsumer.accept(list);
                processedSize.getAndAdd(list.size());
            }
        }
    }

    /**
     * 处理未达到batchSize的元素
     */
    public void post() {
        batchConsumer.accept(new ArrayList<>(queue));
        processedSize.getAndAdd(queue.size());
    }

    /**
     * 获取处理过的总量
     */
    public long processedSize() {
        return processedSize.get();
    }
}
