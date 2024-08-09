package com.kiligz.concurrent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * future任务的处理
 * 支持CompletableFuture
 *
 * @author ivan.zhu
 * @since 2024/7/3
 */
@Data
@SuppressWarnings("all")
public class FutureTasks<T> {
    List<CompletableFuture<T>> cfList = new ArrayList<>();

    /**
     * 任务future结果
     */
    public void add(CompletableFuture<T> cf) {
        cfList.add(cf);
    }

    /**
     * 等待所有任务执行完成
     */
    public void awaitAll() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[]{}));
        allFutures.get();
    }

    /**
     * 等待任一任务执行完成
     */
    public void awaitAny() throws ExecutionException, InterruptedException {
        CompletableFuture.anyOf(cfList.toArray(new CompletableFuture[]{})).get();
    }

    /**
     * 获取所有结果
     */
    public List<T> getAll() throws ExecutionException, InterruptedException {
        List<T> res = new ArrayList<>();
        for (CompletableFuture<T> cf : cfList) {
            res.add(cf.get());
        }
        return res;
    }

    /**
     * 获取任一任务结果
     */
    public T getAny() throws ExecutionException, InterruptedException {
        return (T) CompletableFuture.anyOf(cfList.toArray(new CompletableFuture[]{})).get();
    }
}
