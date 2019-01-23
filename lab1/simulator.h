#ifndef _LAB1_SIMULATOR_H_
#define _LAB1_SIMULATOR_H_

#include "event_handlers.h"

/*
 * Simulator properties provided during invocation
 * */
typedef struct SIM_PROPS_S {

	/*
	 * average number of packets generated [packets/s]
	 * */
	double lambda;

	/*
	 * average length of packet [bits]
	 * arg
	 * */
	double L;

	/*
	 * average number of observer events [bits/s]
	 * */
	double alpha;

	/*
	 * transmission rate of output link [bits/s]
	 * arg
	 * */
	double C;

	/*
	 * utilization, input rate over service rate [packets?]
	 * */
	double rho;

#ifdef FINITE_BUFFER
	/*
	 * buffer size [bits??]
	 * */
	int buffer_size;
#endif

} SIM_PROPS_T;
extern SIM_PROPS_T simulator_options;


/*
 * Stats about queue state that are collected during events
 * */
typedef struct SYS_STATS_S {

	/*
	 * Na – number of arrivals
	 * */
	unsigned long int packets_in;

	/*
	 * Nd – number of departures
	 * */
	unsigned long int packets_out;

	/*
	 * number of packets dropped, used in calculating Ploss?
	 * */
	unsigned long int packets_dropped;

	/*
	 * No – number of observations
	 * */
	unsigned long int observations;

	/*
	 * idle time counter, used to calculate proportion of idle time, Pidle?
	 * */
	unsigned long int idle_count;

	/*
	 * total packet count?
	 * */
	unsigned long int packet_count;

} SYS_STATS_T;
extern SYS_STATS_T system_stats;


/*
 * Simulator actions
 * */
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

/*
 * Simulator descriptions
 * */
EVENT_TYPE_T simulator_get_next_event();

double simulator_get_time();

#endif
