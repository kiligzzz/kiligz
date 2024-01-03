package com.kiligz.algorithm;

import java.util.*;

/**
 * Dijkstra算法
 * 求最短路径（可直接获取最短路径的边对象集）
 *
 * @author Ivan
 * @since 2023/3/27
 */
public class Dijkstra<E extends Dijkstra.Edge> {
    /**
     * 默认权重
     */
    public static final int DEFAULT_WEIGHT = 10;

    /**
     * 边数
     */
    private final int edgeCount;

    /**
     * 图，节点 -> （目标节点 -> 到目标节点的权重）
     */
    private final Map<Integer, Map<Integer, Integer>> graph = new HashMap<>();

    /**
     * 路径对应的边
     */
    private final Map<String, E> pathToEdgeMap = new HashMap<>();

    private Dijkstra(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    /**
     * 添加边
     */
    public void addEdge(int source, int dest, int weight) {
        graph.computeIfAbsent(source, k -> new HashMap<>())
                .put(dest, weight);
    }

    /**
     * 添加边对象
     */
    public void addEdge(E edge) {
        if (edge != null) {
            addEdge(edge.getStart(), edge.getEnd(), edge.getWeight());
            pathToEdgeMap.put(getPathKey(edge.getStart(), edge.getEnd()), edge);
        }
    }

    /**
     * 根据边数画出默认图
     */
    public static Dijkstra<Edge> drawGraph(int edgeCount) {
        Dijkstra<Edge> dijkstra = new Dijkstra<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            dijkstra.addEdge(i, i + 1, DEFAULT_WEIGHT);
        }
        return dijkstra;
    }

    /**
     * 根据边数和边列表画出图
     */
    public static <E extends Edge> Dijkstra<E> drawGraph(int edgeCount, List<E> edgeList) {
        Dijkstra<E> dijkstra = new Dijkstra<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            dijkstra.addEdge(i, i + 1, DEFAULT_WEIGHT);
        }
        for (E edge : edgeList) {
            dijkstra.addEdge(edge);
        }
        return dijkstra;
    }

    /**
     * 开始到结尾的最短路径
     */
    public List<Integer> shortestPath() {
        return shortestPath(0, edgeCount);
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

    /**
     * 开始到结尾的最短路径的边的集合
     */
    public List<E> shortestEdgeList() {
        return shortestEdgeList(0, edgeCount);
    }

    /**
     * 最短路径的边的集合
     */
    public List<E> shortestEdgeList(int source, int dest) {
        List<E> edgeList = new ArrayList<>();
        List<Integer> pathList = shortestPath(source, dest);
        for (int i = 0; i < pathList.size() - 1; i++) {
            Integer start = pathList.get(i);
            Integer end = pathList.get(i + 1);
            E edge = pathToEdgeMap.get(getPathKey(start, end));
            if (edge != null) {
                edgeList.add(edge);
            }
        }
        return edgeList;
    }

    /**
     * 获取路径的key
     */
    private String getPathKey(int start, int end) {
        return start + "_" + end;
    }

    /**
     * 边接口
     */
    public interface Edge {
        int getStart();
        int getEnd();
        default int getWeight() {
            return DEFAULT_WEIGHT;
        }
    }
}
