#include "event_handlers.h"
#include "simulator.h"
#include "erv.h"
#include <stdio.h>

EVENT_LOOKUP_T event_lookup[] = {
	EVENT_DEFINITIONS(_DEF_EVENT_LOOKUP)
	{"no_event",0}
};

void packet_arrival_handler() {

	// Generate ERV packet size
	double packet_size = exp_random_variable(1.0/simulator_options.L);
	double departure_time, current_time;

	// Update statistics
	system_stats.packets_in += 1;

	// Get departure time from queue
	// If queue is not empty, departure time is last departure event + transmission time
	// If queue is empty, departure time is last arrival (current time) + transmission time
	current_time = simulator_get_time();
	departure_time = simulator_get_last_time(packet_departure_event);
	if (departure_time < 0) {
		departure_time = current_time;
	}
	departure_time += packet_size/simulator_options.C;

	// Insert arrival event + time into queue
	simulator_insert_event(
		packet_arrival_event,
		current_time + exp_random_variable(simulator_options.lambda));

	// Add drop event or departure event depending on if buffer is full
#ifdef FINITE_BUFFER
	if (system_stats.packets_in - (system_stats.packets_out + system_stats.packets_dropped) > simulator_options.buffer_size) {
		simulator_insert_event(
				packet_drop_event,
				current_time);
	} else {
#endif
		simulator_insert_event(
				packet_departure_event,
				departure_time);
#ifdef FINITE_BUFFER
	}
#endif

}


void packet_drop_handler() {

	// Update statistics
	system_stats.packets_dropped += 1;
}


void packet_departure_handler() {

	// Update statistics
	system_stats.packets_out += 1;
}


void system_observer_handler() {

	// Update statistics
	system_stats.observations += 1;

	// Update idle count or packet count depending on packets present in the queue
	if (system_stats.packets_in == system_stats.packets_out) {
		system_stats.idle_count += 1;
	} else {
		system_stats.packet_count += system_stats.packets_in - (system_stats.packets_out + system_stats.packets_dropped);
	}

	// Insert next observer event
	simulator_insert_event(
		system_observer_event,
		simulator_get_time() + exp_random_variable(simulator_options.alpha));
}

