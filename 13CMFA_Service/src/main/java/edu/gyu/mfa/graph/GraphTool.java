package edu.gyu.mfa.graph;

import edu.gyu.mfa.info.Constant;
import java.util.*;

public class GraphTool {

    public static Map<Integer, List<Integer>> computeGraphNodeNeighbors(Map<String, Integer> sizeEMUAdjacentMap) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for(String key : sizeEMUAdjacentMap.keySet()) {
            String[] splits = key.split(Constant.KEY_SPLITTER);
            int row = Integer.parseInt(splits[0]);
            int col = Integer.parseInt(splits[1]);
            List<Integer> neighbors = result.get(row);
            if(neighbors == null) {
                neighbors = new ArrayList<>();
                result.put(row, neighbors);
            }
            neighbors.add(col);
        }
        return result;
    }

    public static boolean hasPath(int nodeI, int nodeJ, Map<Integer, List<Integer>> nodeNeighborsMap) {
        if(nodeI == nodeJ) {
            return true;
        }
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Boolean> visitedMap = new HashMap<>();
        visitedMap.put(nodeI, true);
        queue.offer(nodeI);
        while (!queue.isEmpty()) {
            int node = queue.poll();
            if(!nodeNeighborsMap.containsKey(node)) {
                continue;
            }
            List<Integer> neighbors = nodeNeighborsMap.get(node);
            for(int neighbor : neighbors) {
                if (!visitedMap.containsKey(neighbor)) {
                     if (neighbor == nodeJ) {
                         return true;
                     }
                     visitedMap.put(neighbor, true);
                     queue.offer(neighbor);
                }
            }
        }
        return false;
    }

}
