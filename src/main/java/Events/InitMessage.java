package Events;

import Components.Node;
import se.sics.kompics.Init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InitMessage extends Init<Node> {
    public String nodeName;
    public List<String> nodes;
    public HashMap<String,Integer> neighbours;
    public List<String> leafs;


    public InitMessage(String nodeName, List<String> nodes, HashMap<String, Integer> neighbours, List<String> leafs) {
        this.nodeName = nodeName;
        this.nodes = nodes;
        this.neighbours = neighbours;
        this.leafs = leafs;
    }
}