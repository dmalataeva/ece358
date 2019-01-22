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
	printf("Usage: %s [-T max_seconds] rho [rho2 ...]\n",cmd);
}

int main(int argc, char *argv[]) {
	float max_time = SIM_T;
	int i = 1;
	float rho;

	// Parameter parsing
	if (argc < 2) {
		usage(argv[0]);
		return 1;
	}
	while ( i < argc ) {
		char *arg = argv[i];
		if (arg[0] == '-') {
			switch(arg[1]){
				case 'T': 
					i = i + 1;
					max_time = atof(argv[i]);
					break;
				default:
					printf("Unknown switch -%c. Aborting.\n", arg[1]);
					return 1;
			}
		} else {
			break;
		}
		i = i + 1;
	}


	printf("rho,pi,po,pd,o,ic,pc,P_IDLE,E[N]\n");
	while (i < argc && (rho = atof(argv[i]))) {

		simulator_init(SIM_L, SIM_C, rho);
		srand(time(0));

		double t = exp_random_variable(simulator_options.lambda);
		// printf("Inserting incoming_packet_event at %lf\n",t);
		simulator_insert_event(packet_arrival_event, t);
		t = exp_random_variable(simulator_options.alpha);
		simulator_insert_event(system_observer_event, t);

		do {
			simulator_advance();
		} while (simulator_get_time() < max_time);

		simulator_clear_queue();

		printf("%f,%lu,%lu,%lu,%lu,%lu,%lu,%lf,%lf\n",
			rho,
			system_stats.packets_in,
			system_stats.packets_out,
			system_stats.packets_dropped,
			system_stats.observations,
			system_stats.idle_count,
			system_stats.packet_count,
			(double)system_stats.idle_count/system_stats.observations,
			(double)system_stats.packet_count/system_stats.observations
			);
		i = i + 1;
	}

	return 0;
}