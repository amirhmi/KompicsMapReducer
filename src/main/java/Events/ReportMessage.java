package Events;


import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportMessage implements KompicsEvent {

    public String src;
    public String dst;
    public HashMap<String, Integer> occurrences;

    public ReportMessage(String src, String dst, HashMap<String, Integer> occurrences) {
        this.src = src;
        this.dst = dst;
        this.occurrences = occurrences;
    }
}
