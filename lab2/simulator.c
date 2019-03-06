#include "event_handlers.h"
#include "simulator.h"
#include <stdio.h>
#include <stdlib.h>

/*
 * Relevant structs used to represent event queue
 * */
struct ll_node {
	struct ll_node *next;
	struct ll_node *type_next;
	EVENT_TYPE_T event;
	double time;
};

struct linked_list {
	struct ll_node *head, *cursor, *tail;
};

static struct linked_list all_events = {0,0,0};

#define _DEF_LISTS(e,h) {0,0,0},
static struct linked_list event_lists[] = {
	EVENT_DEFINITIONS(_DEF_LISTS)
	{0,0,0}
};

static struct linked_list *node_events; // pointer to array of LLs, one for each node

SIM_PROPS_T simulator_options = {0};
SYS_STATS_T system_stats = {0};

void simulator_init(
	int N,
	float A,
	float L,
	float R,
	float S,
	int D
){
	int node_array[N] = {0};
	struct linked_list node_e[N] = {{0,0,0}};

	simulator_options.N = N;
	simulator_options.A = A;
	simulator_options.L = L;
	simulator_options.R = R;
	simulator_options.S = S;
	simulator_options.D = D;
	simulator_options.tProp = D/S;
	simulator_options.tTrans = L/R;

	system_stats.packets_transmitted = 0;
	system_stats.packet_count = 0;
	system_stats.collision_count = node_array; // array of counters for all nodes
	//memcpy(a, (int[]){1,2,3,4,5}, initlen*sizeof(int));

	node_events = node_e; // hopefully this works haha iwkms
}

void simulator_clear_queue() {
	int i = first_event;
	while (++i != last_event)
		event_lists[i].head = event_lists[i].cursor = event_lists[i].tail = 0;
	struct ll_node *curr = all_events.head;
	while (curr) {
		struct ll_node *next = curr->next;
		free(curr);
		curr = next;
	}
	all_events.head = all_events.cursor = all_events.tail = 0;
}

void simulator_insert_event(int t, EVENT_TYPE_T event_id, double time)
{
	struct ll_node *curr;

	// initialize the node to be inserted
	struct ll_node *in_node = malloc(sizeof(struct ll_node));
	in_node->event = event_id;
	in_node->time = time;

	// insert into event type list
	if (event_lists[event_id].tail && time > event_lists[event_id].tail->time){
		curr = event_lists[event_id].tail;
	} else if (event_lists[event_id].cursor && time > event_lists[event_id].cursor->time) {
		curr = event_lists[event_id].cursor;
	} else {
		curr = event_lists[event_id].head;
	}

	// Insert into typed list
	while (curr && curr->type_next && curr->type_next->time <= time) {
		curr = curr->type_next;
	}
	if (!curr) {

		// First event of its type
		event_lists[event_id].head = in_node;
		event_lists[event_id].tail = in_node;
		in_node->type_next = 0;
		curr = all_events.head;
	} else {
		if (curr == event_lists[event_id].head && curr->time > time) {

			// Insert at head
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
	event_lists[event_id].cursor = in_node;


	if (all_events.tail && time >= all_events.tail->time){
		curr = all_events.tail;
	} else if (all_events.cursor && time > all_events.cursor->time && all_events.cursor->time > curr->time) {
		curr = all_events.cursor;
	} else {
		curr = all_events.head;
	}

	// Insert into generic list
	while (curr && curr->next && curr->next->time <= time) {
		curr = curr->next;
	}
	if (!curr) {

		// First event in list
		all_events.head = in_node;
		all_events.tail = in_node;
		in_node->next = 0;
		curr = all_events.head;
	} else {
		if (curr == all_events.head && curr->time > time) {

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
	all_events.cursor = in_node;

}

/*
 * Process current event and advance queue
 * */
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
	if (curr == all_events.cursor)
		all_events.cursor = curr->next;
	if (!curr->next)
		all_events.tail = 0;
	event_lists[curr->event].head = curr->type_next;
	if (curr == event_lists[curr->event].cursor)
		event_lists[curr->event].cursor = curr->type_next;
	if (!curr->type_next)
		event_lists[curr->event].tail = 0;
	free(curr);
}

/*
 * Get time of last event occurrence
 * */
double simulator_get_last_time(EVENT_TYPE_T event_id){
	struct ll_node *this = event_lists[event_id].tail;
	if (this) {
		return this->time;
	}
	return -1.0;
}

/*
 * Simulator descriptions
 * */
EVENT_TYPE_T simulator_get_next_event(){
    return first_event;
}

/*
 * Get time of current event occurrence
 * */
double simulator_get_time(){
	struct ll_node *this = all_events.head;
	if (this) {
		return this->time;
	}
	return -1.0;
}