package main.java.util;

public class TestRandomGenerator {

    public static final String testERV = "TEST_ERV";
    public static final String testEB = "TEST_EB";

    // Testing RandomGenerator according to Exponential Distribution definitions
    public static void main(String[] args) {
        if (args.length < 3 || (!args[0].equals(testEB) && !args[0].equals(testERV))) {
            System.out.println("Not enough arguments provided");
            System.out.println("Example args for ERV test: TEST_ERV 1000 75");
            System.out.println("Example args for EB test: TEST_EB 1000 16 0.001");
            return;
        }

        if (args[0].equals(testERV)) {
            testExponentialRandomVariable(args);
        } else if (args[0].equals(testEB) && args.length == 4) {
            testExponentialBackoff(args);
        }
    }

    public static void testExponentialRandomVariable(String[] args) {
        int var_count;
        double lambda;

        var_count = Integer.parseInt(args[1]);
        lambda =  Double.parseDouble(args[2]);

        double[] vars = new double[var_count];
        for (int i=0; i<var_count; i++) {
            vars[i] = RandomGenerator.exp_random_variable(lambda);
        }

        double mean = 0;
        for (int i=0; i<var_count; i++) {
            mean += vars[i]/var_count;
        }

        double variance = 0;
        for (int i=0; i<var_count; i++) {
            variance += Math.pow(vars[i]-mean, 2)/var_count;
        }

        System.out.println("Expected Mean: " + 1/lambda);
        System.out.println("Mean: " + mean);
        System.out.println("Expected Variance: " + 1/Math.pow(lambda, 2));
        System.out.println("Variance: " + variance);

        /*
         * Expected values:
         * Mean: lambda^-1
         * Variance: lambda^-2
         */
    }

    public static void testExponentialBackoff(String[] args) {
        int var_count;
        int counter;
        double bit_time;

        var_count = Integer.parseInt(args[1]);
        counter = Integer.parseInt(args[2]);
        bit_time =  Double.parseDouble(args[3]);

        double[] vars = new double[var_count];
        for (int i=0; i<var_count; i++) {
            vars[i] = RandomGenerator.exp_backoff_period(counter, bit_time);
        }

        double mean = 0;
        for (int i=0; i<var_count; i++) {
            mean += vars[i]/var_count;
        }

        double variance = 0;
        for (int i=0; i<var_count; i++) {
            variance += Math.pow(vars[i]-mean, 2)/var_count;
        }

        double b = (Math.pow(2, counter)-1)*bit_time*512;
        double a = 0;

        System.out.println("Expected Mean: " + (a+b)/2);
        System.out.println("Mean: " + mean);
        System.out.println("Expected Variance: " + (Math.pow((b-a+1), 2)-1)/12);
        System.out.println("Variance: " + variance);

        /*
         * Expected values:
         * Mean: (a+b)/2
         * Variance: ((b-a+1)^2 - 1)/12
         *
         * a = 0
         * b = 2^i-1*bit_time*512
         *
         * Set bit_time to 1/512 for std test
         */
    }
}
