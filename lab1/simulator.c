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

#define _DEF_LISTS(e,h) {0,0},
static struct linked_list event_lists[] = {
	EVENT_DEFINITIONS(_DEF_LISTS)
	{0,0}
};


void simulator_init(){}

void simulator_insert_event(EVENT_TYPE_T event_id, double time)
{
	struct ll_node *curr = event_lists[event_id].head;

	struct ll_node *in_node = malloc(sizeof(struct ll_node));
	in_node->event = event_id;
	in_node->time = time;

	// Insert into typed list
	while (curr && curr->type_next && curr->type_next->time < time) {
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
		}
	}

	// Insert into generic list
	while (curr && curr->next && curr->next->time < time) {
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
		}
	}

}

void simulator_advance()
{

}

double simulator_get_last_time(EVENT_TYPE_T event_id){return 0.0;}

// Simulator descriptions
EVENT_TYPE_T simulator_get_next_event(){return first_event;}
double simulator_get_time(){
	struct ll_node *this = all_events.head;
	if (this) {
		return this->time;
	}
	return 0.0;
}