package main.java.simulator;

import javafx.util.Pair;
import main.java.event.*;
import main.java.util.RandomGenerator;

import java.util.*;

public class Simulator {

    /*
     * Mode of operation for CSMA/CD
     * Can be Non-persistent [NONPERSISTENT] or Persistent [PERSISTENT]
     * */
    public String mode;

    public static String NONPERSISTENT = "NONPERSISTENT";
    public static String PERSISTENT = "PERSISTENT";

    /*
     * Number of nodes in network
     * */
    public int N;

    /*
     * Packet arrival rate [packets/s]
     * */
    public double A;

    /*
     * Packet length [bits]
     * */
    public int L;

    /*
     * Link speed [bps]
     * */
    public double R;

    /*
     * Propagation speed [m/s]
     * */
    public double S;

    /*
     * Distance between nodes [m]
     * */
    public double D;

    /*
     * Propagation delay, determined by D/S [s]
     * */
    public double propagation_delay;

    /*
     * Transmission delay, determined by L/R [s]
     * */
    public double transmission_delay;

    /*
     * Total number of packets transmitted
     * */
    public long successful_attempt_count;

    /*
     * Total number of packets queued up
     * */
    // TODO: change to increment once packet is processed --> to avoid counting future packets beyond T_max
    public long attempt_count;

    // test
    public long packets_dropped;

    /*
     * Collision count for exponential back-off
     * */
    public int[] collision_count;

    /*
     * Busy busy count for exponential back-off
     * */
    public int[] bus_busy_count;

    /*
     * Priority queue containing all scheduled events
     * */
    public PriorityQueue<Event> all_events;

    /*
     * Array of events tied by nodeId of the node they happen at
     * */
    public Event[] node_events;

    /*public static Comparator<Event> chronologicalComparator = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.time > e2.time) return 1;
            else if (e1.time < e2.time) return -1;
            return 0;
        }
    };*/

    public Simulator(int N, double A, int L, double R, double S, double D, String type) {
        this.mode = type;

        this.N = N;
        this.A = A;
        this.L = L;
        this.R = R;
        this.S = S;
        this.D = D;
        this.propagation_delay = D/S;
        this.transmission_delay = L/R;

        this.successful_attempt_count = 0;
        this.attempt_count = 0;
        this.packets_dropped = 0;

        this.collision_count = new int[N];
        if (mode.equals(NONPERSISTENT)) {
            this.bus_busy_count = new int[N];
        }

        all_events = new PriorityQueue<>(N, (Event e1, Event e2)-> {
            if (e1.time > e2.time) return 1;
            else if (e1.time <= e2.time) return -1;
            return 0;
        });
        node_events = new Event[N];
    }

    public void clear_queues() {
        all_events.clear();
        node_events = null;
    }

    // TODO: refactor how type of event is infered!
    public void insertEvent(String eventType, int nodeId, double time, List<Delay>... affectedNodes) {
        Event newEvent = null;

        switch (eventType) {
            case Event.ArrivalType:
                newEvent = new Arrival(time, nodeId);
                break;
            case Event.CollisionType:
                if (affectedNodes.length < 1 || affectedNodes[0] == null) {
                    System.out.println("Did not provide full list of affected nodes for collision!");
                }

                newEvent = new Collision(nodeId, time, affectedNodes[0]);
                break;
        }

        if (newEvent != null) {
            all_events.add(newEvent);
            if (eventType.equals(Event.ArrivalType)) node_events[newEvent.nodeId] = newEvent;
        }
    }

    public void advance() {
        Event e = all_events.peek();

        if (e == null) {
            System.out.println("No more events in queue!");
            return;
        }

        switch (e.getType()) {
            case Event.ArrivalType:

                // Increment here to avoid counting packets with time > T_max
                //packet_count++;
                handleArrival((Arrival)e);
                break;
            case Event.CollisionType:
                handleCollision((Collision)e);
                break;
            default:
                System.out.println("Could not recognize type of event being processed");
        }
    }

    public double get_current_time() {
        Event current = all_events.peek();
        return current != null ? current.time : -1;
    }

    private void handleArrival(Arrival a) {
        boolean collisionsWithCurrentNode = processCollisions(a);

        if (!collisionsWithCurrentNode) {
            attempt_count++;
            successful_attempt_count++;

            collision_count[a.nodeId] = 0;
            if (mode.equals(NONPERSISTENT)) bus_busy_count[a.nodeId] = 0;

            insertEvent(Event.ArrivalType, a.nodeId, a.time + RandomGenerator.exp_random_variable(A));

            // change to poll() once confirm working state
            all_events.remove(a);
        }
    }

    private void handleCollision(Collision c) {

        /*if (c.affectedNodes.size() > 2) {
            System.out.println("Detected multicollision");
        }*/

        for (Delay d:c.affectedNodes) {
            if (d.reason.equals(Delay.COLLISION)) {

                // Increment attempt for all nodes that experience transmission collision
                if (collision_count[d.id] > 10) {
                    dropPacket(d.id);
                    collision_count[d.id] = 0;
                } else {
                    attempt_count++;
                    changeEventTime(d.id, d.timeDelay);
                }

            } else if (d.reason.equals(Delay.BUSBUSY)) {
                if (mode.equals(PERSISTENT)) {
                    changeEventTime(d.id, d.timeDelay);

                } else if (mode.equals(NONPERSISTENT)) {
                    if (bus_busy_count[d.id] > 10) {
                        dropPacket(d.id);
                        bus_busy_count[d.id] = 0;
                    } else {
                        changeEventTime(d.id, d.timeDelay);
                    }
                }
            }

        }

        all_events.remove(c);
    }

    private void changeEventTime(int id, double time) {
        Event target = node_events[id];

        if (!all_events.contains(target)) {
            System.out.println("Event queue does not contain the target event!");
            return;
        }

        all_events.remove(target);
        // may pose problems if more than 1 event per node in queue
        target.time = time;
        all_events.add(target);
    }

    private void dropPacket(int nodeId) {
        packets_dropped++;
        all_events.remove(node_events[nodeId]);

        insertEvent(Event.ArrivalType, nodeId, node_events[nodeId].time + RandomGenerator.exp_random_variable(A));
    }

    // return false for no collisions, true for collisions
    private boolean processCollisions(Event e) {
        List<Delay> affectedNodes = new ArrayList<>();
        boolean collisionsWithCurrentNode = false;

        for (int i=0;i<node_events.length; i++) {
            if (node_events[i].equals(e) || node_events[i] == null) {
                continue;
            }

            if (node_events[i].time < Math.abs(e.nodeId-i)*propagation_delay + e.time) {
                collisionsWithCurrentNode = true;
                collision_count[i]++;

                affectedNodes.add(
                        new Delay(
                                i,
                                Delay.COLLISION,
                                node_events[i].time + RandomGenerator.exp_backoff_period(collision_count[i], 1/R)));

            } else if (node_events[i].time > Math.abs(e.nodeId-i)*propagation_delay + e.time
                    && node_events[i].time < Math.abs(e.nodeId-i)*propagation_delay + e.time + transmission_delay) {
                if (mode.equals(PERSISTENT)) {
                    affectedNodes.add(
                        new Delay(
                                i,
                                Delay.BUSBUSY,
                                e.time + Math.abs(e.nodeId-i)*propagation_delay + transmission_delay
                                ));

                } else if (mode.equals(NONPERSISTENT)) {
                    bus_busy_count[i]++;

                    affectedNodes.add(
                        new Delay(
                                i,
                                Delay.BUSBUSY,
                                node_events[i].time + RandomGenerator.exp_backoff_period(bus_busy_count[i], 1/R)));
                }

            }
        }

        // Transmitting node is affected too
        if (collisionsWithCurrentNode) {
            collision_count[e.nodeId]++;

            affectedNodes.add(
                    new Delay(
                            e.nodeId,
                            Delay.COLLISION,
                            e.time + RandomGenerator.exp_backoff_period(collision_count[e.nodeId], 1/R)));
        }

        // Insert collision event if time comparison yielded results
        if (affectedNodes.size() > 0) {
            insertEvent(Event.CollisionType, e.nodeId, get_current_time(), affectedNodes);
        }

        return collisionsWithCurrentNode;
    }

}
