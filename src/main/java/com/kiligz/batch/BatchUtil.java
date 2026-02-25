package com.kiligz.batch;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 批处理工具类
 *
 * @author ivan.zhu
 * @since 2024/10/11
 */
@Slf4j
public class BatchUtil {
    /**
     * 按指定大小分批
     */
    public static <T> List<List<T>> bySize(Collection<T> c, int batchSize) {
        List<T> list = new ArrayList<>(c);
        return Stream.iterate(0, i -> i + batchSize)
                .limit((list.size() + batchSize - 1) / batchSize)
                .map(i -> list.subList(i, Math.min(i + batchSize, list.size())))
                .collect(Collectors.toList());
    }

    /**
     * 分成指定批次
     */
    public static <T> List<List<T>> byCount(Collection<T> c, int batchCount) {
        int batchSize = (int) Math.ceil((double) c.size() / batchCount);
        return bySize(c, batchSize);
    }
}
