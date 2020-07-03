package Events;

import se.sics.kompics.KompicsEvent;

import java.util.List;

public class RoutingTableMessage implements KompicsEvent {
    public String id;
    public String src;
    public String dst;
    public List<Integer> routingTable;

    public RoutingTableMessage(String id, String src, String dst, List<Integer> routingTable) {
        this.id = id;
        this.src = src;
        this.dst = dst;
        this.routingTable = routingTable;
    }
}
