package main.java.event;

public class Arrival extends Event{
    public double arrivalTime;

    public Arrival(double processingTime, double arrivalTime, int nodeId) {
        if (arrivalTime >= processingTime) {
            this.time = arrivalTime;
        } else {
            this.time = processingTime;
        }

        this.arrivalTime = arrivalTime;
        this.nodeId = nodeId;
    }

    public String getType() {
        return "ARRIVAL";
    }
}
