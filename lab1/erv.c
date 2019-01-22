#include "erv.h"
#include <sys/types.h>
#include <stdlib.h>
#include <math.h>

// consts for exponential value gen
// #define LAMBDA 75

// consts for uniform value gen
// #define ERV_RAND_MAX 1000
// #define RAND_RANGE_MIN 1
// #define RAND_RANGE_MAX RAND_MAX


double uniform_random_variable() {
    // double range = (double)(RAND_RANGE_MAX - RAND_RANGE_MIN + 1);
    // double size = (double)(ERV_RAND_MAX + 1) / range;
    // double last = size * range;
    
    // // keep re-generating until less than last chunk ending
    // int var;
    // do {
    //     var = rand();
    // } while (var > last-1);
    
    // return ((double)(RAND_RANGE_MIN + var) / size)/RAND_RANGE_MAX;
    return rand()/((float)RAND_MAX + 1);
}

// exp random variable using inverse method & Poisson distribution function
double exp_random_variable(double lambda) {
    double u = uniform_random_variable();
    // Use this to detect a random number of 1.0
    //if ((double)1.0-u == (double)0.0) printf("zero here\n");
    return log((double)1.0-u)*(-1.0/lambda);
}
