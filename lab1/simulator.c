#include "event_handlers.h"
#include "simulator.h"
#include <stdio.h>
#include <stdlib.h>

struct ll_node {
	struct ll_node *next;
	struct ll_node *type_next;
	EVENT_TYPE_T event;
	double time;
};

struct linked_list {
	struct ll_node *head, *tail;
};
static struct linked_list all_events = {0,0};
// static struct linked_list insertion_order = {0,0};

#define _DEF_LISTS(e,h) {0,0},
static struct linked_list event_lists[] = {
	EVENT_DEFINITIONS(_DEF_LISTS)
	{0,0}
};

SIM_PROPS_T simulator_options = {0};
SYS_STATS_T system_stats = {0};

void simulator_init(float L, float C, float rho){
	simulator_options.L = L;
	simulator_options.C = C;
	simulator_options.rho = rho;
	simulator_options.lambda = (rho * C)/L;
	simulator_options.alpha = simulator_options.lambda * 5;

	system_stats.packets_in = 0;
	system_stats.packets_out = 0;
	system_stats.packets_dropped = 0;
	system_stats.observations = 0;
	system_stats.idle_count = 0;
	system_stats.packet_count = 0;
}

void simulator_clear_queue() {
	int i = first_event;
	while (++i != last_event)
		event_lists[i].head = event_lists[i].tail = 0;
	struct ll_node *curr = all_events.head;
	while (curr) {
		struct ll_node *next = curr->next;
		free(curr);
		curr = next;
	}
	all_events.head = all_events.tail = 0;
}

void simulator_insert_event(EVENT_TYPE_T event_id, double time)
{
	// printf("Inserting event %d at time %f\n",event_id,time);
	struct ll_node *curr = event_lists[event_id].head;

	struct ll_node *in_node = malloc(sizeof(struct ll_node));
	in_node->event = event_id;
	in_node->time = time;

	// Insert into typed list
	while (curr && curr->type_next && curr->type_next->time <= time) {
		curr = curr->type_next;
	}
	if (!curr) {
		// First event of its type
		// printf("Only event of type %d in list.\n",event_id);
		event_lists[event_id].head = in_node;
		event_lists[event_id].tail = in_node;
		in_node->type_next = 0;
		curr = all_events.head;
	} else {
		if (curr == event_lists[event_id].head && curr->time > time) {
			// Insert at head
			// printf("Earliest event of type %d in list.\n",event_id);
			in_node->type_next = curr;
			event_lists[event_id].head = in_node;
			curr = all_events.head;
		} else {
			in_node->type_next = curr->type_next;
			curr->type_next = in_node;
			if (curr == event_lists[event_id].tail)
				event_lists[event_id].tail = in_node;
		}
	}

	// Insert into generic list
	while (curr && curr->next && curr->next->time <= time) {
		curr = curr->next;
	}
	if (!curr) {
		// First event in list
		// printf("Only event in list.\n");
		all_events.head = in_node;
		all_events.tail = in_node;
		in_node->next = 0;
		curr = all_events.head;
	} else {
		if (curr == all_events.head && curr->time > time) {
			// printf("Earliest event in list.\n");
			// Insert at head
			in_node->next = curr;
			all_events.head = in_node;
			curr = all_events.head;
		} else {
			in_node->next = curr->next;
			curr->next = in_node;
			if (curr == all_events.tail)
				all_events.tail = in_node;
		}
	}

}

void simulator_advance()
{
	struct ll_node *curr = all_events.head;
	if (curr != event_lists[curr->event].head){
		printf("Wonky queue...\n");
		printf("[ ");
		struct ll_node *c = all_events.head;
		while (c){
			printf("%p:%d ",c,c->event);
			c = c->next;
		}
		printf("]\n[ ");
		c = event_lists[curr->event].head;
		while (c){
			printf("%p:%d ",c,c->event);
			c = c->type_next;
		}
		printf("]\n");
	}
	EVENT_TYPE_T e = curr->event;
	event_lookup[e].handler();
	all_events.head = curr->next;
	if (!curr->next)
		all_events.tail = 0;
	event_lists[curr->event].head = curr->type_next;
	if (!curr->type_next)
		event_lists[curr->event].tail = 0;
	free(curr);
}

double simulator_get_last_time(EVENT_TYPE_T event_id){
	struct ll_node *this = event_lists[event_id].tail;
	if (this) {
		return this->time;
	}
	return -1.0;
}

// Simulator descriptions
EVENT_TYPE_T simulator_get_next_event(){return first_event;}
double simulator_get_time(){
	struct ll_node *this = all_events.head;
	if (this) {
		return this->time;
	}
	return -1.0;
}