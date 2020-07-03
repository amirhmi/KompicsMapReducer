package Components;


import Events.InitMessage;
import Ports.EdgePort;
import misc.Edge;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class App extends ComponentDefinition {

    String startNode = "";
    ArrayList<Edge> edges = new ArrayList<>();
    Map<String,Component> components = new HashMap<String,Component>();
    int dist[][];
    final int INF = 99999;

    public App()
    {
        readTable();
    }

    public static void main(String[] args) {
        Kompics.createAndStart(App.class);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.exit(1);
        }
        Kompics.shutdown();
    }

    public void read_file_create_edges()
    {
        File resourceFile = new File("src/main/java/mst.txt");
        try (Scanner scanner = new Scanner(resourceFile)) {
            int i = 0;
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                int weight = Integer.parseInt(line.split(",")[1]);
                String rel = line.split(",")[0];
                String src = rel.split("-")[0];
                String dst = rel.split("-")[1];
                edges.add(new Edge(src,dst,weight));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getNodes()
    {
        List<String> nodes = new ArrayList<>();
        for(Edge edge:edges) {
            if(!nodes.contains(edge.src))
                nodes.add(edge.src);
            if(!nodes.contains(edge.dst))
                nodes.add(edge.dst);
        }
        return nodes;
    }


    public void readTable(){

        read_file_create_edges();

        for(Edge edge:edges){
            if (!components.containsKey(edge.src)){
                Component c = create(Node.class,new InitMessage(edge.src, getNodes(),
                        NodeUtils.findNeighbours(edges, edge.src), NodeUtils.findLeafs(edges)));
                components.put(edge.src,c) ;
            }
            if (!components.containsKey(edge.dst)){
                Component c = create(Node.class,new InitMessage(edge.dst, getNodes(),
                        NodeUtils.findNeighbours(edges, edge.dst), NodeUtils.findLeafs(edges)));
                components.put(edge.dst,c) ;
            }
            connect(components.get(edge.src).getPositive(EdgePort.class),
                    components.get(edge.dst).getNegative(EdgePort.class), Channel.TWO_WAY);
            connect(components.get(edge.src).getNegative(EdgePort.class),
                    components.get(edge.dst).getPositive(EdgePort.class),Channel.TWO_WAY);
        }
    }
}
