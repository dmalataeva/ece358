#ifndef _LAB1_ERV_H_
#define _LAB1_ERV_H_

/*
 * Function Prototypes for Poisson distribution generation
 * */

// Generate a URV in the range [0,1)
double uniform_random_variable();

// Generate ERV based on provided lambda value using inverse method & Poisson distribution function
double exp_random_variable(double lambda);

#endif
