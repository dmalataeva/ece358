#ifndef _LAB1_EVENT_HANDLERS_H_
#define _LAB1_EVENT_HANDLERS_H_

/*
 * Event definitions for Discrete Event Simulations
 *
 * An abstracted implementation to look up events and their respective handlers
 * */

#define EVENT_DEFINITIONS(_p) \
	_p(packet_arrival_event, packet_arrival_handler) \
	_p(exp_backoff_event, exp_backoff_handler)

// Define global names for the events
#define _DEF_EVENTS(e,h) e,
typedef enum EVENT_TYPE_E {
	EVENT_DEFINITIONS(_DEF_EVENTS)
	last_event,
	first_event = -1
} EVENT_TYPE_T;

#define _DEF_HANDLERS(e,h) void h();
EVENT_DEFINITIONS(_DEF_HANDLERS)

typedef struct EVENT_LOOKUP_S {
	char name[25];
	void (*handler)();
} EVENT_LOOKUP_T;
#define _DEF_EVENT_LOOKUP(e,h) {#e,h},
extern EVENT_LOOKUP_T event_lookup[];

#endif
