CC = gcc
CFLAGS = -I. -lm
DEPS = erv.h event_handlers.h simulator.h

all: erv_test simulate_mm1 simulate_mm1k

clean:
	rm erv_test simulate_mm1k simulate_mm1
	rm *.o

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

%-f.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS) -DFINITE_BUFFER

erv_test: test_erv.o erv.o
	$(CC) -o $@ $^ $(CFLAGS)

simulate_mm1: main.o erv.o event_handlers.o simulator.o
	$(CC) -o $@ $^ $(CFLAGS)

simulate_mm1k: main-f.o erv-f.o event_handlers-f.o simulator-f.o
	$(CC) -o $@ $^ $(CFLAGS)

.PHONY: all clean
