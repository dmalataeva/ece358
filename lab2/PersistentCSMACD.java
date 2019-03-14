import java.util.PriorityQueue;
import java.util.Random;

public class PersistentCSMACD {

    /*
     * Number of nodes in network
     * N_arg_start denotes first value of N
     * N_arg_end denotes last value of N
     * N_step denotes the step for incrementing N
     * */
    public static int N_arg_start = 10;
    public static int N_arg_end = 10;
    public static int N_step = 1;

    /*
     * Packet arrival rate [packets/s]
     * */
    public static double A = 10;

    /*
     * Packet length [bits]
     * */
    public static int L = 1500;

    /*
     * Link speed [bps]
     * */
    public static double R = 1000000;

    /*
     * Propagation speed [m/s]
     * */
    public static double S = 200000000;

    /*
     * Distance between nodes [m]
     * */
    public static double D = 10;

    /*
     * Simulation time [s]
     * */
    public static double T_max = 1000;

    public static void main(String[] args) {
        parseInput(args);

        System.out.println("N       Efficiency      Throughput");

        for (int i_N=N_arg_start; i_N<=N_arg_end; i_N+=N_step) {
            Simulator simulator = new Simulator(i_N, A, L, R, S, D);

            for (int i=0; i<i_N; i++) {
                simulator.insertEvent(i, 0, exp_random_variable(simulator.A));
            }

            do {
                simulator.advance();
            } while (simulator.get_current_time() < T_max);

            System.out.println(i_N + ",   " + (double)simulator.successful_attempt_count/simulator.attempt_count
                    + ",  " + (double)simulator.successful_attempt_count*L/(T_max*1000000));
        }
    }

    private static void parseInput(String[] args) {
        for (int i=1; i<args.length; i+=2) {
            switch (args[i-1]) {
                case "-T":
                    T_max = Double.parseDouble(args[i]);
                    break;
                case "-R":
                    R = Double.parseDouble(args[i]);
                    break;
                case "-S":
                    S = Double.parseDouble(args[i]);
                    break;
                case "-D":
                    D = Double.parseDouble(args[i]);
                    break;
                case "-L":
                    L = Integer.parseInt(args[i]);
                    break;
                case "-A":
                    A = Double.parseDouble(args[i]);
                    break;
                case "-N":
                    if (args[i].contains("[") && args[i].contains("]")) {
                        String[] N_args = args[i].split("[\\[\\]]", 3);
                        N_arg_start = Integer.parseInt(N_args[0]);
                        N_step = Integer.parseInt(N_args[1]);
                        N_arg_end = Integer.parseInt(N_args[2]);
                        break;
                    } else if (!args[i].contains("[") && !args[i].contains("]")) {
                        N_arg_start = N_arg_end = Integer.parseInt(args[i]);
                        break;
                    }
                default:
                    System.out.println("Could not parse arguments!");
            }
        }
    }

    private static class Simulator {

            // Packet arrival rate [packets/s]
            private double A;

            // Link speed [bps]
            private double R;

            // Propagation delay, determined by D/S [s]
            private double propagation_delay;

            // Transmission delay, determined by L/R [s]
            private double transmission_delay;

            // Total number of successful transmissions
            private long successful_attempt_count;

            // Total number of attempted transmissions
            private long attempt_count;

            // Collision count for exponential back-off
            private int[] collision_count;

            // Priority queue containing all scheduled events
            private PriorityQueue<Arrival> all_events;

            // Array of events tied by nodeId of the node they happen at
            private Arrival[] node_events;

            public Simulator(int N, double A, int L, double R, double S, double D) {
                this.A = A;
                this.R = R;

                this.propagation_delay = D/S;
                this.transmission_delay = L/R;

                this.successful_attempt_count = 0;
                this.attempt_count = 0;

                this.collision_count = new int[N];

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
                            a.arrivalTime + exp_random_variable(A));

                    all_events.remove(a);
                    collision_count[a.nodeId] = 0;
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
                all_events.remove(node_events[nodeId]);
                Arrival a = node_events[nodeId];

                insertEvent(nodeId, a.processingTime, a.arrivalTime + exp_random_variable(A));
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
                                            + exp_backoff_period(collision_count[i], 1/R));
                        }

                        // check for bus busy
                    } else if (node_events[i].processingTime >= Math.abs(a.nodeId-i)*propagation_delay + a.processingTime
                            && node_events[i].processingTime <= Math.abs(a.nodeId-i)*propagation_delay + a.processingTime + transmission_delay) {
                        changeEventTime(i,
                                a.processingTime
                                        + Math.abs(a.nodeId-i)*propagation_delay
                                        + transmission_delay);
                    }
                }

                // For persistent mode, implement bus busy delay for all nodes
                // this delay has meaning for nodes that "see" a collision, but were not involved
                for (int i = 0; i < node_events.length; i++) {
                    changeEventTime(i,
                            a.processingTime // should be discoveryDelay
                                    + transmission_delay
                                    + Math.abs(a.nodeId - i) * propagation_delay);
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
                                        + exp_backoff_period(collision_count[a.nodeId], 1/R));
                    }
                }

                return collisionsWithCurrentNode;
            }
    }

    private static class Arrival{

        /*
         * The time when the packet arrives in the node's queue
         * */
        private double arrivalTime;

        /*
         * The time when the packet is processed
         * */
        private double processingTime;

        /*
         * Id of the node the packet belongs to
         * */
        private int nodeId;

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


    /*
     * Random variable generation functions
     * */

    public static double uniform_random_variable() {
        return Math.random();
    }

    // generate random uniformly distributed int between 0 and upperBound (excluded)
    public static int uniform_random_int(int upperBound) {
        Random r = new Random();
        return r.nextInt(upperBound);
    }

    // generate random exponentially distributed double
    public static double exp_random_variable(double lambda) {
        return Math.log(1.0 - uniform_random_variable())*(-1.0/lambda);
    }

    // generate exponential backoff period according to CSMA/CD
    public static double exp_backoff_period(int i, double bit_time) {
        return uniform_random_int((int)Math.pow(2, i))*bit_time*512;
    }
}
