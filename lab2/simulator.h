#ifndef _LAB1_SIMULATOR_H_
#define _LAB1_SIMULATOR_H_

#include "event_handlers.h"

/*
 * Simulator properties provided during invocation
 * */
typedef struct SIM_PROPS_S {

	/*
 	 * number of nodes in simulated network
 	 * arg
 	 * */
	double N;

	/*
	 * packet arrival rate [packets/s]
	 * arg
	 * */
	double A;

	/*
	 * packet length [bits]
	 * */
	double L;

	/*
	 * transmission rate of output link [bits/s]
	 * */
	double R;

	/*
	 * propagation speed of medium [m/s]
	 * arg
	 * */
	double S;

	/*
	 * distance between nodes
	 * */
	double D;

	/*
	 * propagation delay for a single link [s]
	 * */
	double tProp;

	/*
	 * transmission delay for a single link [s]
	 * */
	double tTrans;

	// ==============================================================


	/*
	 * average number of packets generated [packets/s]
	 * */
	//double lambda;

	/*
	 * average number of observer events
	 * */
	//double alpha;

	/*
	 * utilization, input rate over service rate [packets?]
	 * */
	//double rho;

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
	 * number of successful packet transmissions
	 * */
	unsigned long int packets_transmitted;

	/*
	 * total packet count
	 * */
	unsigned long int packet_count;

    /*
     * collision counter for all nodes
     * */
    unsigned int *collision_count;


	// ==============================================================

	/*
	 * number of arrivals
	 * */
	//unsigned long int packets_in;

	/*
	 * number of departures
	 * */
	//unsigned long int packets_out;

	/*
	 * number of packets dropped, used in calculating probability of loss
	 * */
	//unsigned long int packets_dropped;

	/*
	 * number of observations
	 * */
	//unsigned long int observations;

	/*
	 * idle observer counter, used to calculate proportion of idle time
	 * */
	//unsigned long int idle_count;

} SYS_STATS_T;
extern SYS_STATS_T system_stats;


/*
 * Simulator actions
 * */
void simulator_init(
#ifdef FINITE_BUFFER
	int b_size,
#endif
	int N,
	float A,
	float L,
	float R,
	float S,
	int D
	);

void simulator_clear_queue();

void simulator_insert_event(int,EVENT_TYPE_T, double);

void simulator_advance();

double simulator_get_last_time(EVENT_TYPE_T);

/*
 * Simulator descriptions
 * */
EVENT_TYPE_T simulator_get_next_event();

double simulator_get_time();

#endif
