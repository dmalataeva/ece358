#include "simulator.h"
#include "event_handlers.h"
#include "erv.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

void usage(char *cmd) {
	printf("Usage: %s [-l lambda] max_seconds\n",cmd);
}

int main(int argc, char *argv[]) {
	float max_time = 0, lambda=75;

	// Parameter parsing
	if (argc < 2) {
		usage(argv[0]);
		return 1;
	}
	{
		int i = 1;
		while ( i < argc ) {
			char *arg = argv[i];
			if (arg[0] == '-') {
				switch(arg[1]){
					case 'l': 
						i = i + 1;
						lambda = atoi(argv[i]);
						break;
					default:
						printf("Unknown switch -%c. Aborting.\n", arg[1]);
						return 1;
				}
			} else {
				max_time = atoi(arg);
				if (max_time == 0) {
					printf("Bad max_time: %s. Aborting.\n",arg);
					return 1;
				}
			}
			i = i + 1;
		}
	}
	if (max_time == 0) {
		printf("No max_time provided. Aborting.\n");
		return 1;
	}

	simulator_init();
	srand(time(0));

	double t = exp_random_variable(lambda);
	while (t < max_time) {
		printf("Inserting incoming_packet_event at %lf\n",t);
		t = t + exp_random_variable(lambda);
	}

	return 0;
}