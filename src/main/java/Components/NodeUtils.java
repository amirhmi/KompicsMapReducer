package Components;

import misc.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodeUtils {
    final static int INF = 99999;

    public static String findMinAverageNode(int[][] dist)
    {
        int minDist = INF;
        char minNode = 0;
        for(int i=0; i<dist.length; i++) {
            int row = 0;
            for(int j=0; j<dist.length; j++)
                row += dist[i][j];
            if(row < minDist) {
                minDist = row;
                minNode = (char) ('A' + i);
            }
        }
        return Character.toString(minNode);
    }

    public static HashMap<String, Integer> findReducerOnEachNeighbor(String node, List<String> leafs,
                                                               HashMap<String,Integer> neighbours,
                                                               int dist[][])
    {
        HashMap<String, Integer> reducer = new HashMap<>();
        for(String neighbor:neighbours.keySet()) {
            reducer.put(neighbor, 0);
            for (String leaf : leafs)
                if (dist[charToInt(node)][charToInt(leaf)] > dist[charToInt(neighbor)][charToInt(leaf)])
                    reducer.put(neighbor, reducer.get(neighbor) + 1);
        }
        return reducer;
    }

    public static int charToInt(String c)
    {
        return c.charAt(0) - 'A';
    }

    public static List<String> findLeafs(List<Edge> edges)
    {
        List<String> leafs = new ArrayList<>();
        for(Edge edge:edges) {
            if(!leafs.contains(edge.src) & findNeighbours(edges, edge.src).size() == 1)
                leafs.add(edge.src);
            if(!leafs.contains(edge.dst) & findNeighbours(edges, edge.dst).size() == 1)
                leafs.add(edge.dst);
        }
        return leafs;
    }

    public static HashMap<String,Integer> findNeighbours(List<Edge> edges, String node){
        HashMap<String,Integer> nb = new HashMap<String,Integer>();
        for(Edge tr:edges){
            if(tr.src.equalsIgnoreCase(node) && !nb.containsKey(tr.dst)){
                nb.put(tr.dst , tr.weight);
            }
            else if (tr.dst.equalsIgnoreCase(node) && !nb.containsKey(tr.src)){
                nb.put(tr.src , tr.weight);
            }
        }
        return nb;
    }
}
