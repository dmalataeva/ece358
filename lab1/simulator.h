#ifndef _LAB1_SIMULATOR_H_
#define _LAB1_SIMULATOR_H_

#include "event_handlers.h"

// Simulator actions
void simulator_init();
void simulator_insert_event(EVENT_TYPE_T, double);
void simulator_advance();
double simulator_get_last_time(EVENT_TYPE_T);

// Simulator descriptions
EVENT_TYPE_T simulator_get_next_event();
double simulator_get_time();


#endif
