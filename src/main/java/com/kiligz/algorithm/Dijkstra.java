package com.kiligz.algorithm;

import java.util.*;

/**
 * Dijkstra算法
 * 求最短路径
 *
 * @author Ivan
 * @since 2023/3/27
 */
public class Dijkstra<T> {
    /**
     * 默认权重
     */
    public static final int DEFAULT_WEIGHT = 10;

    /**
     * 图
     */
    private final Map<T, Map<T, Integer>> graph;

    public Dijkstra() {
        this.graph = new HashMap<>();
    }

    public Dijkstra(Map<T, Map<T, Integer>> graph) {
        this.graph = graph;
    }

    /**
     * 添加边
     */
    public void addEdge(T source, T dest, int weight) {
        graph.computeIfAbsent(source, k -> new HashMap<>())
                .put(dest, weight);
    }

    /**
     * 到指定目标节点的最短路径
     */
    public Integer shortestPath(T source, T destination) {
        return shortestPath(source).get(destination);
    }

    /**
     * 所有目标节点的最短路径
     */
    public Map<T, Integer> shortestPath(T source) {
        // 目标节点 -> 源节点到目标节点的距离
        Map<T, Integer> destToDistanceMap = new HashMap<>();
        // 已经遍历过的节点
        Set<T> visited = new HashSet<>();
        // 目标节点优先队列，按源节点到目标点的距离从小到大排
        PriorityQueue<T> queue = new PriorityQueue<>(Comparator.comparingInt(destToDistanceMap::get));

        // 初始化
        destToDistanceMap.put(source, 0);
        queue.add(source);

        // 循环直到graph遍历完
        while (!queue.isEmpty()) {
            T cur = queue.poll();
            if (!visited.add(cur)) continue;

            // 目标节点 -> 当前节点到目标节点的权重
            Map<T, Integer> destToWeightMap = graph.getOrDefault(cur, new HashMap<>());
            destToWeightMap.forEach((dest, weight) -> {
                // 当前节点到新目标节点的距离
                int newDistance = destToDistanceMap.get(cur) + weight;
                // 记录最短路径
                Integer oldDistance = destToDistanceMap.get(dest);
                if (oldDistance == null || oldDistance > newDistance) {
                    destToDistanceMap.put(dest, newDistance);
                    queue.add(dest);
                }
            });
        }
        return destToDistanceMap;
    }

}
