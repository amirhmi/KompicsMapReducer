package Ports;

import Events.PathMessage;
import Events.ReportMessage;
import Events.RoutingMessage;
import Events.RoutingTableMessage;
import se.sics.kompics.PortType;

public class EdgePort extends PortType {{
    positive(PathMessage.class);
    positive(RoutingTableMessage.class);
    positive(RoutingMessage.class);
    positive(ReportMessage.class);
    negative(PathMessage.class);
    negative(RoutingTableMessage.class);
    negative(RoutingMessage.class);
    negative(ReportMessage.class);
}}
