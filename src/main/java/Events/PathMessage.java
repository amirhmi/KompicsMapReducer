package Events;

import se.sics.kompics.KompicsEvent;

import java.util.List;

public class PathMessage implements KompicsEvent {
    public String id;
    public String src;
    public String dst;
    public int dist;
    public boolean isRespond;
    public List<String> respondPath;

    public PathMessage(String id, String src, String dst,
                       int dist, boolean isRespond, List<String> respondPath) {
        this.id = id;
        this.src = src;
        this.dst = dst;
        this.dist = dist;
        this.isRespond = isRespond;
        this.respondPath = respondPath;
    }
}
