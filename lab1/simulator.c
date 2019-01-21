#include "event_handlers.h"
#include "simulator.h"
#include <stdio.h>

void simulator_init(){}
void simulator_insert_event(EVENT_TYPE_T event_id, double time){}
void simulator_advance(){}
double simulator_get_last_time(EVENT_TYPE_T event_id){return 0.0;}

// Simulator descriptions
EVENT_TYPE_T simulator_get_next_event(){return first_event;}
double simulator_get_time(){return 0.0;}