package com.kiligz.algorithm;

import java.util.*;

/**
 * Dijkstra算法
 * 求最短路径
 *
 * @author Ivan
 * @since 2023/3/27
 */
public class Dijkstra {
    /**
     * 默认权重
     */
    public static final int DEFAULT_WEIGHT = 10;

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
     * 根据边数画出默认图
     */
    public static Dijkstra drawGraph(int edgeCount) {
        Dijkstra dijkstra = new Dijkstra();
        for (int i = 0; i < edgeCount; i++) {
            dijkstra.addEdge(i, i + 1, DEFAULT_WEIGHT);
        }
        return dijkstra;
    }

    /**
     * 最短路径
     */
    public List<Integer> shortestPath(int source, int dest) {
        Map<Integer, Integer> destToDistanceMap = new HashMap<>();
        Map<Integer, Integer> nodeToPrevMap = new HashMap<>();
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingInt(destToDistanceMap::get));
        Set<Integer> visited = new HashSet<>();

        // 初始化
        destToDistanceMap.put(source, 0);
        queue.add(source);

        // 计算最短路径
        while (!queue.isEmpty()) {
            Integer cur = queue.poll();
            if (cur.equals(dest)) break;
            visited.add(cur);

            graph.get(cur).forEach((node, weight) -> {
                if (!visited.contains(node)) {
                    Integer newDistance = destToDistanceMap.get(cur) + weight;
                    Integer oldDistance = destToDistanceMap.get(node);
                    if (oldDistance == null || newDistance < oldDistance) {
                        destToDistanceMap.put(node, newDistance);
                        nodeToPrevMap.put(node, cur);
                        queue.add(node);
                    }
                }
            });
        }

        // 构造路径
        List<Integer> path = new ArrayList<>();
        Integer cur = dest;
        while (nodeToPrevMap.containsKey(cur)) {
            path.add(cur);
            cur = nodeToPrevMap.get(cur);
        }
        path.add(source);
        Collections.reverse(path);

        return path;
    }
}
