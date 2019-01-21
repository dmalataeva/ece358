#ifndef _LAB1_EVENT_HANDLERS_H_
#define _LAB1_EVENT_HANDLERS_H_

/* Event definitions for Discrete Event Simulations */

#define EVENT_DEFINITIONS(_p) \
	_p(packet_arrival_event, packet_arrival_handler) \
	_p(packet_drop_event, packet_drop_handler) \
	_p(packet_departure_event, packet_departure_handler) \
	_p(system_observer_event, system_observer_handler)

// Define global names for the events
#define _DEF_EVENTS(e,h) e,
typedef enum EVENT_TYPE_E {
	EVENT_DEFINITIONS(_DEF_EVENTS)
	last_event,
	first_event = -1
} EVENT_TYPE_T;

#define _DEF_HANDLERS(e,h) void h();
EVENT_DEFINITIONS(_DEF_HANDLERS)

#endif
