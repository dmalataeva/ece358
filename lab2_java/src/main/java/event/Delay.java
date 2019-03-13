package main.java.event;

public class Delay {

    /*
     * Reason for time delay
     * Can be either due to collision [COLLISION] or bus busy [BUSBUSY]
     */
    public String reason;

    public static String BUSBUSY = "BUSBUSY";
    public static String COLLISION = "COLLISION";
    public static String COLLISIONWAIT = "COLLISIONWAIT";

    /*
     * Used to identify node on the network bus
     */
    public int id;

    /*
     * The delay for a collided packet's arrival
     * Expressed as the absolute new time value that
     * the arrival needs to be set to
     */
    public Double timeDelay;

    // might need separate initializer w/o event
    public Delay(int id, String reason, double timeDelay) {
        this.id = id;
        this.reason = reason;
        this.timeDelay = timeDelay;
    }
}
