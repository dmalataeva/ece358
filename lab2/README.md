# ECE358 Lab 1 Instructions

## Compilation

The project provides a makefile to build all of the necessary executables.
The default target generates all of the execuatbles, but the following targets
can be used to build individual exectuables:

- erv_test
- simulate_mm1
- simulate_mm1k

Ex. `make simulate_mm1 simulate_mm1k` compiles the two simulation executabels,
but not the erv test.

## Running

The two simulator executables will provide a usage message when run with no
parameters, but the erv test does not.  In either event, the exact commands to
run the code are broken down in the following sections.

### ERV Test

The ERV test has only one parameter, lambda, and it is positional. The
following snippet will run the ERV test with its default value of 75.

`erv_test 75`

The expected output is:
```
Mean: 0.013154
Variance: 0.000192
```

The numbers will not be the same, but will be close to expected 1/lambda and
1/(lambda^2).

### MM1 Queue Simulation

Running the MM1 Queue executable without arguments will result in the following
message:

```
Usage: ./simulate_mm1 [-T max_seconds] [-C transmit_speed] [-L avg_packet_size] rho [rho2 ...]
```

The arguments will default to T=1000, C=1000000, and L=2000 as in the manual.
The positional argument of rho allows you to provide as many rho values as you
would like to test.

Ex. `./simulate_mm1 -T 1000 0.25 0.35 0.45 0.{5..9}5` will run the entire range
of tests for the first set of tests from the manual.  That is, an MM1
simulation of T=1000s sweeping rho from 0.25 to 0.95 in increments of 0.1.

The expected result is a list of comma seperated values for the results of
the simulation:

```
rho,P_IDLE,E[N]
0.250000,0.751703,0.331237
0.350000,0.650619,0.537438
0.450000,0.551090,0.817777
0.550000,0.451134,1.222564
0.650000,0.351373,1.855121
0.750000,0.248706,3.025848
0.850000,0.147759,5.797864
0.950000,0.049426,19.099749
```

### MM1K Queue Simulation

Running the MM1 Queue executable without arguments will result in the following
message:

```
Usage: ./simulate_mm1k [-T max_seconds] [-C transmit_speed] [-L avg_packet_size] [-K buffer_size] rho [rho2 ...]
```

The arguments will default to T=1000, C=1000000, and L=2000 as in the manual.
The buffer size, K, will default to 10. The positional argument of rho allows
you to provide as many rho values as you would like to test.

Ex. `./simulate_mm1k -T 1000 0.5 0.6 0.7 0.8 0.9 1.{0..5}` will run the entire range
of tests for the first set of tests from the manual.  That is, an MM1K
simulation of T=1000s sweeping rho from 0.5 to 1.5 in increments of 0.1.

The expected result is a list of comma seperated values for the results of
the simulation:

```
rho,P_IDLE,E[N],P_LOSS
0.500000,0.006467,0.990983,0.000424
0.600000,0.000263,1.457430,0.002321
0.700000,0.000145,2.115397,0.008539
0.800000,0.000046,2.939859,0.023589
0.900000,0.000022,3.944618,0.050286
1.000000,0.000018,4.975571,0.089895
1.100000,0.000002,5.918994,0.139959
1.200000,0.000015,6.723183,0.193776
1.300000,0.000000,7.310749,0.243762
1.400000,0.000000,7.764203,0.292413
1.500000,0.000004,8.133464,0.338809
```

To run all of the tests from the simulation, use the following commands:
```
./simulate_mm1k -K 10 -T 1000 0.5 0.6 0.7 0.8 0.9 1.{0..5}
./simulate_mm1k -K 25 -T 1000 0.5 0.6 0.7 0.8 0.9 1.{0..5}
./simulate_mm1k -K 50 -T 1000 0.5 0.6 0.7 0.8 0.9 1.{0..5}
```
