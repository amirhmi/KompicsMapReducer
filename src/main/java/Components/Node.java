package Components;
import Events.*;
import Ports.EdgePort;
import org.apache.commons.io.FileUtils;
import se.sics.kompics.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Node extends ComponentDefinition {
    Positive<EdgePort> recievePort = positive(EdgePort.class);
    Negative<EdgePort> sendPort = negative(EdgePort.class);
    Boolean isRoot = false;
    int reported = 0;
    String nodeName;
    String parentName;
    HashMap<String, Integer> reducer;
    HashMap<String, Integer> occurrences = new HashMap<>();
    List<String> leafs;

    List<String> nodes;
    int pathRespond;
    List<Integer> ownDist = new ArrayList<>();
    int dist[][];
    int respondTables = 1;


    HashMap<String,Integer> neighbours = new HashMap<>();

    Handler reportHandler = new Handler<ReportMessage>() {
        @Override
        public void handle(ReportMessage event) {
            if(!event.dst.equalsIgnoreCase(nodeName))
                return;
            for(Map.Entry<String, Integer> entry:event.occurrences.entrySet()) {
                String word = entry.getKey();
                int repeatNum = entry.getValue();
                if(occurrences.containsKey(word))
                    occurrences.put(word, occurrences.get(word)+ repeatNum);
                else
                    occurrences.put(word, repeatNum);
            }
            reported++;
            if (isRoot & reported == neighbours.size()) {
                writeDataToFile(occurrences);
                System.out.println("data saved in result.txt");
            }
            else if(!isRoot & reported == neighbours.size() - 1) {
                System.out.println("Node: " + nodeName + " all data gotten from neighbors");
                trigger(new ReportMessage(nodeName, parentName, occurrences), sendPort);
            }
        }
    };

    Handler routeHandler = new Handler<RoutingMessage>() {
        @Override
        public void handle(RoutingMessage event) {
            if(!event.dst.equalsIgnoreCase(nodeName))
                return;
            parentName = event.src;
            if(neighbours.size() == 1) {
                System.out.println(event.fileData.length);
                for (String word : event.fileData) {
                    if (occurrences.containsKey(word))
                        occurrences.put(word, occurrences.get(word) + 1);
                    else
                        occurrences.put(word, 1);
                }
                System.out.println("Leaf:" + nodeName + " reading file finished");
                trigger(new ReportMessage(nodeName, parentName, occurrences),sendPort);
            }
            else {
                HashMap<String, String[]> neighborFile = divideFileForNeighbors(event.fileData);
                for( Map.Entry<String, Integer> entry : neighbours.entrySet())
                {
                    if(entry.getKey().equalsIgnoreCase(parentName))
                        continue;
                    System.out.println("Node: " + nodeName + " send file to neighbor " + entry.getKey());
                    trigger(new RoutingMessage(nodeName, entry.getKey(),
                            neighborFile.get(entry.getKey())),sendPort);
                }
            }
        }
    };


    Handler pathHandler = new Handler<PathMessage>() {
        @Override
        public void handle(PathMessage event) {
            if(!event.dst.equalsIgnoreCase(nodeName)) {
                if (event.isRespond) {
                    if(event.respondPath.contains(nodeName))
                        return;
                    List<String> path = event.respondPath;
                    path.add(nodeName);
                    trigger(new PathMessage(event.id, event.src, event.dst,
                                    event.dist, true, path)
                            , sendPort);
                }
                return;
            }


            if(event.isRespond & event.id.equalsIgnoreCase(nodeName)) {
                pathRespond++;
                ownDist.set(charToInt(event.src), event.dist);
                if(pathRespond == nodes.size() - 1) {
                    for (int i = 0; i < nodes.size(); i++)
                        dist[charToInt(nodeName)][i] = ownDist.get(i);

                    for( Map.Entry<String, Integer> entry : neighbours.entrySet())
                        trigger(new RoutingTableMessage(event.id, nodeName, entry.getKey(), ownDist)
                                ,sendPort);
                }
            }
            if(!event.isRespond) {
                List<String> path = new ArrayList<>();
                path.add(nodeName);
                trigger(new PathMessage(event.id, nodeName, event.id,
                                event.dist, true, path)
                        ,sendPort);
                for( Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                    if(entry.getKey().equalsIgnoreCase(event.src))
                        continue;
                    trigger(new PathMessage(event.id, nodeName, entry.getKey(),
                                    event.dist+entry.getValue(), false, null)
                            ,sendPort);
                }
            }
        }
    };

    Handler routingTableHandler = new Handler<RoutingTableMessage>() {
        @Override
        public void handle(RoutingTableMessage event) {
            if(!event.dst.equalsIgnoreCase(nodeName))
                return;

            for (int i = 0; i < event.routingTable.size(); i++)
                dist[charToInt(event.id)][i] = event.routingTable.get(i);

            for (Map.Entry<String, Integer> entry : neighbours.entrySet())
                if (!entry.getKey().equalsIgnoreCase(event.src))
                    trigger(new RoutingTableMessage(event.id, nodeName, entry.getKey(),
                                    event.routingTable)
                            , sendPort);

            respondTables++;
            if(respondTables == nodes.size())
                reducer = NodeUtils.findReducerOnEachNeighbor(nodeName, leafs, neighbours, dist);

            if (respondTables == nodes.size() & NodeUtils.findMinAverageNode(dist).equalsIgnoreCase(nodeName)) {
                isRoot = true;

                System.out.println("##################################");
                for (int i = 0; i < nodes.size(); i++) {
                    for (int j = 0; j < nodes.size(); j++)
                        System.out.print(dist[i][j] + "\t");
                    System.out.println("");
                }
                System.out.println("##################################");

                String[] fileData = readFile("CA-text-file");
                HashMap<String, String[]> neighborFile = divideFileForNeighbors(fileData);
                System.out.println("input file read in root");
                for( Map.Entry<String, Integer> entry : neighbours.entrySet())
                {
                    trigger(new RoutingMessage(nodeName, entry.getKey(),
                            neighborFile.get(entry.getKey())),sendPort);
                }

            }
        }
    };

    Handler startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            dist = new int[nodes.size()][nodes.size()];
            for(int i=0; i<nodes.size(); i++)
                ownDist.add(0);
            for( Map.Entry<String, Integer> entry : neighbours.entrySet()) {
                trigger(new PathMessage(nodeName, nodeName, entry.getKey(), entry.getValue(), false, null)
                        ,sendPort);
            }
        }
    };

    public Node(InitMessage initMessage) {
        nodeName = initMessage.nodeName;
        System.out.println("initNode :" + initMessage.nodeName);
        this.neighbours = initMessage.neighbours;
        this.nodes = initMessage.nodes;
        this.leafs = initMessage.leafs;
        subscribe(startHandler, control);
        subscribe(pathHandler ,recievePort);
        subscribe(routingTableHandler ,recievePort);
        subscribe(reportHandler,recievePort);
        subscribe(routeHandler,recievePort);
    }

    private HashMap<String, String[]> divideFileForNeighbors(String[] data)
    {
        int leafSize = 0;
        for(Map.Entry entry:reducer.entrySet()){
            if(entry.getKey().equals(parentName))
                continue;
            leafSize += (int)entry.getValue();
        }
        int chunckSize = data.length / leafSize;
        int pointer = 0;
        HashMap<String, String[]> dataMap = new HashMap<>();
        for(Map.Entry entry:reducer.entrySet()){
            if(entry.getKey().equals(parentName))
                continue;
            int value = (int)entry.getValue();
            dataMap.put((String)entry.getKey(), Arrays.copyOfRange(data, pointer*chunckSize,
                    (pointer+value)*chunckSize));
            pointer += value;
            if(pointer == leafSize)
                dataMap.put((String)entry.getKey(), Arrays.copyOfRange(data, (pointer-value)*chunckSize,
                        data.length));
        }
        return dataMap;
    }

    private String[] readFile(String file)
    {
        try {
            File dataFile = new File("CA-text-file");
            String[] data = FileUtils.readFileToString(dataFile, StandardCharsets.UTF_8)
                    .toLowerCase().split("[^a-zA-Z]");
            return data;
        } catch (IOException e) {
            return null;
        }
    }

    private void writeDataToFile(HashMap<String, Integer> data)
    {
        try {
            PrintWriter writer = new PrintWriter("result.txt");
            writer.print("");
            for(Map.Entry<String, Integer> entry:sortByValue(data).entrySet()) {
                if(entry.getKey().equalsIgnoreCase(""))
                    continue;
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
            writer.close();
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private int charToInt(String c)
    {
        return c.charAt(0) - 'A';
    }

}

