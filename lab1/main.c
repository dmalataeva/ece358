#include "simulator.h"
#include "event_handlers.h"
#include "erv.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#define SIM_C 1000000
#define SIM_L 2000
#define SIM_T 1000

void usage(char *cmd) {
	printf("Usage: %s [-T max_seconds] [-C transmit_speed] [-L avg_packet_size]",cmd);
	#ifdef FINITE_BUFFER
	printf(" [-K buffer_size]");
	#endif
	printf(" rho [rho2 ...]\n");
}

int main(int argc, char *argv[]) {
	float max_time = SIM_T;
	float tx_speed = SIM_C, p_size_avg = SIM_L;
#ifdef FINITE_BUFFER
	int buf_size = 10;
#endif
	int i = 1;
	float rho;

	if (argc < 2) {
		usage(argv[0]);
		return 1;
	}
	while ( i < argc ) {
		char *arg = argv[i];
		if (arg[0] == '-') {
			switch(arg[1]){
				case 'C': 
					tx_speed = atof(argv[++i]);
					break;
				case 'L': 
					p_size_avg = atof(argv[++i]);
					break;
				case 'T': 
					max_time = atof(argv[++i]);
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


	printf("rho,P_IDLE,E[N]");
#ifdef FINITE_BUFFER
	printf(",P_LOSS");
#endif
	printf("\n");
	while (i < argc && (rho = atof(argv[i]))) {

		simulator_init(
#ifdef FINITE_BUFFER
			buf_size,
#endif
			p_size_avg,
			tx_speed,
			rho);
		srand(time(0));

		double t = exp_random_variable(simulator_options.lambda);

		simulator_insert_event(packet_arrival_event, t);
		t = exp_random_variable(simulator_options.alpha);
		simulator_insert_event(system_observer_event, t);

		do {
			simulator_advance();
		} while (simulator_get_time() < max_time);

		simulator_clear_queue();

		printf("%f,%lf,%lf",
			rho,
			(double)system_stats.idle_count/system_stats.observations,
			(double)system_stats.packet_count/system_stats.observations
			);
#ifdef FINITE_BUFFER
		printf(",%lf",
			(double)system_stats.packets_dropped/system_stats.packets_in);
#endif
		printf("\n");
		i = i + 1;
	}

	return 0;
}