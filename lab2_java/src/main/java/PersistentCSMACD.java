package main.java;

import main.java.event.Event;
import main.java.simulator.Simulator;
import main.java.util.RandomGenerator;

public class PersistentCSMACD {
    public static double T_arg = 1000;
    public static double R_arg = 1000000;
    public static double S_arg = 200000000;
    public static double D_arg = 10;
    public static int L_arg = 1500;
    public static double A_arg = 10;
    public static int N_arg_start = 10;
    public static int N_arg_end = 10;
    public static int N_step = 1;

    public static void main(String[] args) {
        parse_args(args);

        System.out.println("N       Efficiency      Throughput");

        for (int i_N=N_arg_start; i_N<=N_arg_end; i_N+=N_step) {
            Simulator simulator = new Simulator(i_N, A_arg, L_arg, R_arg, S_arg, D_arg, Simulator.PERSISTENT);

            for (int i=0; i<i_N; i++) {
                simulator.insertEvent(Event.ArrivalType, i, RandomGenerator.exp_random_variable(simulator.A));
            }

            do {
                simulator.advance();
            } while (simulator.get_current_time() < T_arg);

            //System.out.println("packets transmitted: " + simulator.successful_attempt_count + "\npacket total: " + simulator.attempt_count
            //        + "\npackets dropped: " + simulator.packets_dropped);

            System.out.println(i_N + ",   " + (double)simulator.successful_attempt_count/simulator.attempt_count
                    + ",  " + (double)simulator.successful_attempt_count*simulator.L/(T_arg*1000000));
        }
    }

    private static void parse_args(String[] args) {

        // TODO: rewrite logic for param parsing to allow defaulting
        for (int i=1; i<args.length; i+=2) {
            switch (args[i-1]) {
                case "-T":
                    T_arg = Double.parseDouble(args[i]);
                    break;
                case "-R":
                    R_arg = Double.parseDouble(args[i]);
                    break;
                case "-S":
                    S_arg = Double.parseDouble(args[i]);
                    break;
                case "-D":
                    D_arg = Double.parseDouble(args[i]);
                    break;
                case "-L":
                    L_arg = Integer.parseInt(args[i]);
                    break;
                case "-A":
                    A_arg = Double.parseDouble(args[i]);
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

}
