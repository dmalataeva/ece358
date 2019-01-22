#include "erv.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <sys/wait.h>

#define VAR_COUNT 1000
#define LAMBDA 75

int main(int argc, char *argv[]) {
    srand(time(0));

    double nums[VAR_COUNT];
    double lambda = LAMBDA;

    // printf("argc: %d\n",argc);

    if (argc > 1) {
        lambda = atof(argv[1]);
        // printf("argv[1]: %s\n",argv[1]);
    }

    for (int i=0; i<VAR_COUNT; i++) {
        nums[i] = exp_random_variable(lambda);
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
