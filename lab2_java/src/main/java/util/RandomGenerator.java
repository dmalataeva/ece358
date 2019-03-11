package main.java.util;

import java.util.Random;

public class RandomGenerator {
    public static double uniform_random_variable() {
        return Math.random();
    }

    public static int uniform_random_int(int upperBound) {
        Random r = new Random();
        return r.nextInt(upperBound);
    }

    public static double exp_random_variable(double lambda) {
        return Math.log(1.0 - uniform_random_variable())*(-1.0/lambda);
    }

    public static double exp_backoff_period(int i, double bit_time) {
        return uniform_random_int((int)Math.pow(2, i))*bit_time*512;
    }
}
