import java.util.*;
public class Question4b {
    public static int minRoadsToCollectPackages(int[] packages, int[][] roads) {
        int n = packages.length;
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());
        for (int[] road : roads) {
            graph.get(road[0]).add(road[1]);
            graph.get(road[1]).add(road[0]);
        }
        Set<Integer> packageLocations = new HashSet<>();
        for (int i = 0; i < n; i++) if (packages[i] == 1) packageLocations.add(i);

        return dfs(0, -1, graph, packageLocations, new boolean[n]) * 2;
    }
    private static int dfs(int node, int parent, List<List<Integer>> graph, Set<Integer> packageLocations, boolean[] visited) {
        visited[node] = true;
        int totalDistance = 0;
        for (int neighbor : graph.get(node)) {
            if (neighbor != parent && !visited[neighbor]) {
                int subTreeDistance = dfs(neighbor, node, graph, packageLocations, visited);
                if (subTreeDistance > 0 || packageLocations.contains(neighbor)) {
                    totalDistance += subTreeDistance + 1;
                }
            }
        }
        return totalDistance;
    }
    public static void main(String[] args) {
        int[] packages1 = {1, 0, 0, 0, 0, 1};
        int[][] roads1 = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}};
        System.out.println(minRoadsToCollectPackages(packages1, roads1));

        int[] packages2 = {0,0,0,1,1,0,0,1};
        int[][] roads2 = {{0,1},{0,2},{1,3},{1,4},{2,5},{5,6},{5,7}};
        System.out.println(minRoadsToCollectPackages(packages2, roads2));
    }
}
