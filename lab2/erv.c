#include "erv.h"
#include <sys/types.h>
#include <stdlib.h>
#include <math.h>

double uniform_random_variable() {
    return rand()/((double)RAND_MAX + 1);
}

double exp_random_variable(double lambda) {
    double u = uniform_random_variable();
    return log((double)1.0-u)*(-1.0/lambda);
}
