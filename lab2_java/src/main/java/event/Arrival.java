package main.java.event;

public class Arrival extends Event{
    public Arrival(double time, int nodeId) {
        this.time = time;
        this.nodeId = nodeId;
    }

    public String getType() {
        return "ARRIVAL";
    }
}
