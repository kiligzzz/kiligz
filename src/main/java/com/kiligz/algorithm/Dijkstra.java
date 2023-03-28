package com.kiligz.algorithm;

import java.util.*;

/**
 * Dijkstra算法（基于边）
 * 求最短路径
 *
 * @author Ivan
 * @since 2023/3/27
 */
public class Dijkstra {
    /**
     * 默认权重
     */
    public static final int DEFAULT_WEIGHT = 1;

    /**
     * 图，节点 -> （目标节点 -> 到目标节点的权重）
     */
    private final Map<Integer, Map<Integer, Integer>> graph = new HashMap<>();

    /**
     * 添加边
     */
    public void addEdge(int source, int dest, int weight) {
        graph.computeIfAbsent(source, k -> new HashMap<>())
                .put(dest, weight);
    }

    /**
     * 删除边
     */
    public void removeEdge(int source, int dest, int weight) {
        graph.get(source).remove(dest, weight);
    }

    /**
     * 1. 图加入特定边（peekSource,peekDest,weight）
     * 2. 求节点（source）到节点（dest）的最短路径
     * 3. 将图恢复原样
     */
    public int peekShortest(int source, int dest, int peekSource, int peekDest, int weight) {
        addEdge(peekSource, peekDest, weight);
        Integer shortest = shortestPath(source, dest);
        removeEdge(peekSource, peekDest, weight);
        return shortest;
    }

    public static Dijkstra drawGraph(int edgeCount) {
        Dijkstra dijkstra = new Dijkstra();
        for (int i = 0; i < edgeCount; i++) {
            dijkstra.addEdge(i, i + 1, DEFAULT_WEIGHT);
        }
        return dijkstra;
    }

    /**
     * 到指定目标节点的最短路径
     */
    public Integer shortestPath(Integer source, Integer dest) {
        return shortestPath(source).get(dest);
    }

    /**
     * 所有目标节点的最短路径
     */
    public Map<Integer, Integer> shortestPath(Integer source) {
        // 目标节点 -> 源节点到目标节点的距离
        Map<Integer, Integer> destToDistanceMap = new HashMap<>();
        // 已经遍历过的节点
        Set<Integer> visited = new HashSet<>();
        // 目标节点优先队列，按源节点到目标点的距离从小到大排
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(destToDistanceMap::get));

        // 初始化
        destToDistanceMap.put(source, 0);
        queue.add(source);

        // 循环直到graph遍历完
        while (!queue.isEmpty()) {
            Integer cur = queue.poll();
            if (!visited.add(cur)) continue;

            // 目标节点 -> 当前节点到目标节点的权重
            Map<Integer, Integer> destToWeightMap = graph.getOrDefault(cur, new HashMap<>());
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
