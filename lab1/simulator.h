#ifndef _LAB1_SIMULATOR_H_
#define _LAB1_SIMULATOR_H_

#include "event_handlers.h"

typedef struct SIM_PROPS_S {
	double lambda;
	double L;
	double alpha;
	double C;
	double rho;
#ifdef FINITE_BUFFER
	int buffer_size;
#endif
} SIM_PROPS_T;
extern SIM_PROPS_T simulator_options;

typedef struct SYS_STATS_S {
	unsigned long int packets_in;
	unsigned long int packets_out;
	unsigned long int packets_dropped;
	unsigned long int observations;
	unsigned long int idle_count;
	unsigned long int packet_count;
} SYS_STATS_T;
extern SYS_STATS_T system_stats;

// Simulator actions
void simulator_init(
#ifdef FINITE_BUFFER
	int b_size,
#endif
	float L,
	float C,
	float rho
	);
void simulator_clear_queue();
void simulator_insert_event(EVENT_TYPE_T, double);
void simulator_advance();
double simulator_get_last_time(EVENT_TYPE_T);

// Simulator descriptions
EVENT_TYPE_T simulator_get_next_event();
double simulator_get_time();


#endif
