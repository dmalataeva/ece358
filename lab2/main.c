#include "simulator.h"
#include "event_handlers.h"
#include "erv.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#define SIM_R 1000000
#define SIM_S 200000000
#define SIM_D 10
#define SIM_L 1500
#define SIM_A 10
#define SIM_T 1000

void usage(char *cmd) {
	printf("Usage: %s [-T max_seconds] [-R transmission_rate] [-S propagation_speed] [-D node_distance] [-L packet_length] [-A packet_arrival_rate]",cmd);
	#ifdef FINITE_BUFFER
	printf(" [-K buffer_size]");
	#endif
	printf("N [N2 ...]\n");
}

int main(int argc, char *argv[]) {
	float Tmax = SIM_T;
	float R = SIM_R, S = SIM_S, D = SIM_D, L = SIM_L, A = SIM_A;
#ifdef FINITE_BUFFER
	int buf_size = 10;
#endif
	int i = 1, t = 0;
	int N;

	if (argc < 2) {
		usage(argv[0]);
		return 1;
	}
	while ( i < argc ) {
		char *arg = argv[i];
		if (arg[0] == '-') {
			switch(arg[1]){
				case 'T':
					Tmax = atof(argv[++i]);
					break;
				case 'R':
					R = atof(argv[++i]);
					break;
				case 'S':
					S = atof(argv[++i]);
					break;
				case 'D':
					D = atof(argv[++i]);
					break;
				case 'L':
					L = atof(argv[++i]);
					break;
				case 'A':
					A = atof(argv[++i]);
					break;
#ifdef FINITE_BUFFER
				case 'K':
					buf_size = atoi(argv[++i]);
					break;
#endif
				default:
					printf("Unknown switch -%c. Aborting.\n", arg[1]);
					return 1;
			}
		} else {
			break;
		}
		i = i + 1;
	}


	printf("N,efficiency,throughput");
#ifdef FINITE_BUFFER
	printf(",P_LOSS");
#endif
	printf("\n");
	while (i < argc && (N = atof(argv[i]))) {

	    simulator_init(N,A,L,R,S,D);
        srand(time(0));

	    for (t=0; t<N; t++) {
            double arrival_time = exp_random_variable(simulator_options.A);
            simulator_insert_event(t, packet_arrival_event, arrival_time);
	    }

        do {
            simulator_advance();
        } while (simulator_get_time() < Tmax);

	    simulator_clear_queue();

        printf("%d,%lf,%lf",
               N,
               (double)system_stats.packets_transmitted/system_stats.packet_count,
               (double)(system_stats.packets_transmitted*simulator_options.L)/Tmax
        );

        printf("\n");
        i = i + 1;
	}

	return 0;
}