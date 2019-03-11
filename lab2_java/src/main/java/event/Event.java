package main.java.event;

public abstract class Event {
    public double time;
    public int nodeId;

    public static final String ArrivalType = "ARRIVAL";
    public static final String CollisionType = "COLLISION";

    public abstract String getType();
}
