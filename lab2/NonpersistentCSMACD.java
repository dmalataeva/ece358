import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class NonpersistentCSMACD {

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

    // Random generator
    public static Random rand;

    public static void main(String[] args) {
        parseInput(args);

        System.out.println("N       Efficiency      Throughput");

        for (int i_N=N_arg_start; i_N<=N_arg_end; i_N+=N_step) {
            rand = new Random();
            Simulator simulator = new Simulator(i_N, A, L, R, S, D);

            double currentTime = simulator.getNextInTime();

            while (currentTime != -1 && currentTime < T_max) {
                simulator.advance();
                currentTime = simulator.getNextInTime();
            }

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

        // Counter for Non-persistent exponential back-off
        private int[] bus_busy_count;

        // Priority queue containing all scheduled events
        private List<PriorityQueue<Double>> all_events;

        public Simulator(int N, double A, int L, double R, double S, double D) {
            this.A = A;
            this.R = R;

            this.propagation_delay = D/S;
            this.transmission_delay = L/R;

            this.successful_attempt_count = 0;
            this.attempt_count = 0;

            this.collision_count = new int[N];
            this.bus_busy_count = new int[N];

            all_events = new ArrayList<PriorityQueue<Double>>();

            // preschedule all arrivals
            for (int i=0; i<N; i++) {
                PriorityQueue<Double> pq = new PriorityQueue<>();

                double currentTime = 0;
                while (currentTime < T_max) {
                    currentTime += expRandomVariable(A);
                    pq.add(currentTime);
                }

                all_events.add(pq);
            }
        }

        public Double getNextInTime() {
            Double next = -1.0;

            for (int i=0; i<all_events.size(); i++) {
                Double current = all_events.get(i).peek();
                if (next == -1 || (current != -1 && current < next)) {
                    next = current;
                }
            }

            if (next == -1) {
                System.out.println("No more events in queue!");
            }

            return next;
        }

        public int getNextNode() {
            Double next = getNextInTime();

            for (int i=0; i<all_events.size(); i++) {
                if (next == all_events.get(i).peek()) return i;
            }

            return -1;
        }

        // change processing time of next packet in some node
        private void changeEventTime(int id, double newTime) {
            if (all_events.get(id).isEmpty()) {
                System.out.println("Node queue does not contain any more arrivals!");
                return;
            } else if (all_events.get(id).peek() > newTime) {
                // We don't want to change time to an earlier timestamp
                return;
            }

            all_events.get(id).remove();
            all_events.get(id).add(newTime);
        }

        // compare processing time of next packet in each node to sender node, implement delays accordingly
        // returns true if there was a collision, false if none
        private void advance() {
            Double currentArrival = getNextInTime();
            int currentNode = getNextNode();

            boolean collisionsWithCurrentNode = false;
            bus_busy_count[currentNode] = 0;
            double discoveryDelay = transmission_delay;
            attempt_count++; // for current sender node

            for (int i=0;i<all_events.size(); i++) {
                Double peerArrival = all_events.get(i).peek();
                if (currentArrival == peerArrival || peerArrival == null) {
                    continue;
                }

                // check for collision
                if (peerArrival <= Math.abs(currentNode-i)*propagation_delay + currentArrival) {
                    collisionsWithCurrentNode = true;
                    attempt_count++; //for each collision
                    collision_count[i]++;

                    discoveryDelay = Math.min(Math.abs(currentNode-i)*propagation_delay, discoveryDelay);

                    if (collision_count[i] > 10) {
                        all_events.get(i).remove();
                        collision_count[i] = 0;
                    } else {
                        double currentWaitTime = currentArrival
                                + Math.abs(currentNode-i)*propagation_delay
                                + expBackoffPeriod(collision_count[i], 1/R);

                        while (all_events.get(i).peek() < currentWaitTime) {
                            changeEventTime(i, currentWaitTime);
                        }
                    }
                }
            }

            // Transmitting node is affected too
            if (collisionsWithCurrentNode) {
                collision_count[currentNode]++;

                if (collision_count[currentNode] > 10) {
                    all_events.get(currentNode).remove();
                    collision_count[currentNode] = 0;
                } else {
                    double currentWaitTime = currentArrival
                            + discoveryDelay
                            + expBackoffPeriod(collision_count[currentNode], 1/R);

                    while (all_events.get(currentNode).peek() < currentWaitTime) {
                        changeEventTime(currentNode, currentWaitTime);
                    }
                }
            } else {
                successful_attempt_count++;

                all_events.get(currentNode).remove();
                collision_count[currentNode] = 0;
                bus_busy_count[0] = 0;
            }

            for (int i=0; i<all_events.size(); i++) {
                double currentWaitTime = currentArrival
                        + Math.abs(currentNode-i)*propagation_delay
                        + discoveryDelay;

                while (all_events.get(i).peek() < currentWaitTime) {
                    if (bus_busy_count[i] < 10) bus_busy_count[i]++;
                    changeEventTime(i, currentWaitTime + expBackoffPeriod(bus_busy_count[i], 1/R));
                }
            }
        }
    }

    /*
     * Random variable generation functions
     * */

    public static double uniformRandomVariable() {
        return Math.random();
    }

    // generate random uniformly distributed int between 0 and upperBound (excluded)
    public static int uniformRandomInt(int upperBound) {
        return rand.nextInt(upperBound);
    }

    // generate random exponentially distributed double
    public static double expRandomVariable(double lambda) {
        return Math.log(1.0 - uniformRandomVariable())*(-1.0/lambda);
    }

    // generate exponential backoff period according to CSMA/CD
    public static double expBackoffPeriod(int i, double bit_time) {
        return uniformRandomInt((int)Math.pow(2, i))*bit_time*512;
    }
}
