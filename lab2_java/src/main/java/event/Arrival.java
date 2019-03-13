package main.java.event;

public class Arrival{

    /*
     * The time when the packet arrives in the node's queue
     * */
    public double arrivalTime;

    /*
     * The time when the packet is processed
     * */
    public double processingTime;

    /*
     * Id of the node the packet belongs to
     * */
    public int nodeId;

    public Arrival(double processingTime, double arrivalTime, int nodeId) {
        if (arrivalTime >= processingTime) {
            this.processingTime = arrivalTime;
        } else {
            this.processingTime = processingTime;
        }

        this.arrivalTime = arrivalTime;
        this.nodeId = nodeId;
    }
}
