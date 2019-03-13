package main.java.util;

import java.util.Random;

public class RandomGenerator {
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
