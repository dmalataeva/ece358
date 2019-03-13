package main.java.simulator;

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
     * Total number of successful transmissions
     * */
    public long successful_attempt_count;

    /*
     * Total number of attempted transmissions
     * */
    public long attempt_count;

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
    public PriorityQueue<Arrival> all_events;

    /*
     * Array of events tied by nodeId of the node they happen at
     * */
    public Arrival[] node_events;

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

        this.collision_count = new int[N];
        if (mode.equals(NONPERSISTENT)) {
            this.bus_busy_count = new int[N];
        }

        // we use processing time of packet, since this is when transmission attempts happen
        all_events = new PriorityQueue<>(N, (Arrival a1, Arrival a2)-> {
            if (a1.processingTime > a2.processingTime) return 1;
            else if (a1.processingTime <= a2.processingTime) return -1;
            return 0;
        });

        node_events = new Arrival[N];
    }

    // add new packet arrival into queue and update pointers from nodes
    public void insertEvent(int nodeId, double processingTime, double arrivalTime) {
        Arrival newEvent = new Arrival(processingTime, arrivalTime, nodeId);

        all_events.add(newEvent);
        node_events[newEvent.nodeId] = newEvent;
    }

    // get next event in queue but do not remove it yet
    // decoupled from handleArrival() because we used to have different kinds of events
    public void advance() {
        Arrival a = all_events.peek();

        if (a == null) {
            System.out.println("No more events in queue!");
            return;
        }

        handleArrival(a);
    }

    // used to run simulator until max simulation time
    public double get_current_time() {
        Arrival a = all_events.peek();
        return a != null ? a.processingTime : -1;
    }

    // handle arrival; update stats and insert new packet arrival upon success
    private void handleArrival(Arrival a) {
        boolean collisionsWithCurrentNode = processCollisions(a);

        if (!collisionsWithCurrentNode) {
            attempt_count++;
            successful_attempt_count++;

            insertEvent(
                    a.nodeId,
                    a.processingTime + transmission_delay,
                    a.arrivalTime + RandomGenerator.exp_random_variable(A));

            all_events.remove(a);
            collision_count[a.nodeId] = 0;
            if (mode.equals(NONPERSISTENT)) bus_busy_count[a.nodeId] = 0;
        }
    }

    // change processing time of next packet in some node
    private void changeEventTime(int id, double newTime) {
        Arrival target = node_events[id];

        if (!all_events.contains(target)) {
            System.out.println("Event queue does not contain the target event!");
            return;
        } else if (target.processingTime > newTime) {
            // We don't want to change time to an earlier timestamp
            return;
        }

        all_events.remove(target);
        target.processingTime = newTime;
        all_events.add(target);
    }

    // drop packet from a node's queue, schedule a new one since we do not preschedule arrivals
    private void dropPacket(int nodeId) {
        collision_count[nodeId] = 0;
        if (mode.equals(NONPERSISTENT)) bus_busy_count[nodeId] = 0;

        all_events.remove(node_events[nodeId]);

        Arrival a = node_events[nodeId];

        insertEvent(nodeId, a.processingTime, a.arrivalTime + RandomGenerator.exp_random_variable(A));
    }

    // compare processing time of next packet in each node to sender node, implement delays accordingly
    // returns true if there was a collision, false if none
    private boolean processCollisions(Arrival a) {
        boolean collisionsWithCurrentNode = false;

        // used to compute collision backoff for sender node
        double discoveryDelay = transmission_delay;

        for (int i=0;i<node_events.length; i++) {
            if (node_events[i].equals(a) || node_events[i] == null) {
                continue;
            }

            // check for collision
            if (node_events[i].processingTime < Math.abs(a.nodeId-i)*propagation_delay + a.processingTime) {
                collisionsWithCurrentNode = true;
                attempt_count++;
                collision_count[i]++;

                discoveryDelay = Math.min(Math.abs(a.nodeId-i)*propagation_delay, discoveryDelay);

                if (collision_count[i] > 10) {
                    dropPacket(i);
                } else {
                    changeEventTime(i,
                            a.processingTime
                            + Math.abs(a.nodeId-i)*propagation_delay
                            + RandomGenerator.exp_backoff_period(collision_count[i], 1/R));
                }

            // check for bus busy
            } else if (node_events[i].processingTime >= Math.abs(a.nodeId-i)*propagation_delay + a.processingTime
                    && node_events[i].processingTime <= Math.abs(a.nodeId-i)*propagation_delay + a.processingTime + transmission_delay) {
                if (mode.equals(PERSISTENT)) {
                    changeEventTime(i,
                            a.processingTime
                            + Math.abs(a.nodeId-i)*propagation_delay
                            + transmission_delay);
                } else if (mode.equals(NONPERSISTENT)) {
                    bus_busy_count[i]++;

                    if (bus_busy_count[i] > 10) {
                        dropPacket(i);
                    } else {
                        changeEventTime(i,
                                a.processingTime
                                + Math.abs(a.nodeId-i)*propagation_delay
                                + RandomGenerator.exp_backoff_period(bus_busy_count[i], 1/R));
                    }
                }
            }
        }

        // For persistent mode, implement bus busy delay for all nodes
        // this delay has meaning for nodes that "see" a collision, but were not involved
        if (mode.equals(PERSISTENT)) {
            for (int i = 0; i < node_events.length; i++) {
                changeEventTime(i,
                        a.processingTime // should be discoveryDelay
                                + transmission_delay + Math.abs(a.nodeId - i) * propagation_delay);
            }
        }

        // Transmitting node is affected too
        if (collisionsWithCurrentNode) {
            collision_count[a.nodeId]++;
            attempt_count++;

            if (collision_count[a.nodeId] > 10) {
                dropPacket(a.nodeId);
            } else {
                changeEventTime(a.nodeId,
                        a.processingTime
                                + discoveryDelay
                                + RandomGenerator.exp_backoff_period(collision_count[a.nodeId], 1/R));
            }
        }

        return collisionsWithCurrentNode;
    }

}
