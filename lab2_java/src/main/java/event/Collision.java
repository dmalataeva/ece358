package main.java.event;

import java.util.List;

public class Collision extends Event {
    /*
     * List of delays that need to be implemented for nodes
     *
     * For both modes, these nodes will need to undergo a delay
     * and schedule their next packet arrival to current_time+delay
     *
     * The delay time depends on type of "collision" (transmission time collision or bus busy)
     * ---
     * If the reason for delay is packet collision during transmission:
     * For both PERSISTENT & NONPERSISTENT modes, delay is determined by:
     *      exp_random_backoff()
     * ---
     * If the reason for delay is bus busy:
     * For PERSISTENT mode, delay is determined by:
     *      get_current_time() + [# of links]*propagation_delay + transmission_delay
     * For NONPERSISTENT mode, delay is determined by:
     *      exp_random_backoff()
     */
    public List<Delay> affectedNodes;

    public Collision(int nodeId, double time, List<Delay> affectedNodes) {
        this.time = time;
        this.nodeId = nodeId;
        this.affectedNodes = affectedNodes;
    }

    public String getType() {
        return "COLLISION";
    }
}
