
#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <math.h>
#include <time.h>
#include <sys/wait.h>

// consts for exponential value gen
#define LAMBDA 75
#define VAR_COUNT 1000

// consts for uniform value gen
#define ERV_RAND_MAX 1000
#define RAND_RANGE_MIN 1
#define RAND_RANGE_MAX 1000
#define ROUND_DEC 10000000


double uniform_random_variable() {
    double range = (double)RAND_RANGE_MAX - RAND_RANGE_MIN + 1;
    double size = (double)(ERV_RAND_MAX + 1) / range;
    double last = size * range;
    
    // keep re-generating until less than last chunk ending
    int var = rand();
    while (var > last-1) var = rand();
    
    return ((double)(RAND_RANGE_MIN + var) / size)/RAND_RANGE_MAX;
}

// exp random variable using inverse method & Poisson distribution function
double exp_random_variable(double u) {
    
    // Use this to detect a random number of 1.0
    //if ((double)1.0-u == (double)0.0) printf("zero here\n");
    return log((double)1.0-u)*(-1.0/LAMBDA);
}

int main(void) {
    srand(time(0));

    double nums[VAR_COUNT];

    for (int i=0; i<VAR_COUNT; i++) {
        nums[i] = exp_random_variable(uniform_random_variable());
    }
    
    double meanSum = 0.0;
    for (int i=0; i<VAR_COUNT; i++) {
        meanSum += nums[i];
    }
    double mean = meanSum/VAR_COUNT;
    
    double varSum = 0.0;
    for (int i=0; i<VAR_COUNT; i++) {
        varSum += pow(nums[i]-mean, 2);
    }
    double variance = varSum/VAR_COUNT;
    
    printf("Mean: %lf\n", mean);
    printf("Variance: %lf\n", variance);
    // need a mean of LAMBDA^-1 ==> ~ 0.0133333
    // need variance of LAMBDA^-2 ==> ~ 0.000178
    
    return 0;
}
