package Events;

import se.sics.kompics.KompicsEvent;

public class RoutingMessage implements KompicsEvent {
    public String src;
    public String dst;
    public String[] fileData;

    public RoutingMessage(String src, String dst, String[] fileData) {
        this.src = src;
        this.dst = dst;
        this.fileData = fileData;
    }
}
