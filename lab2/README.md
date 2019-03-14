Lab 2: CSMA/CD Performance Evaluation

### To compile: ###
Run `make`.

Alternatively, `javac PersistentCSMACD.java NonpersistentCSMACD.java`.

### To run: ###
`java PersistentCSMACD [-T simulation time] [-R transmission speed] [-S propagation speed] 
[-D internode distance] [-L packet length] [-A packet rate] [-N number of nodes]`

#### To run with default params: ####
`java PersistentCSMACD`

#### Example, from question 1 in report (need to run separately for each A value): ####
`java PersistentCSMACD -T 1000 -R 1000000 -S 200000000 -D 10 -L 1500 -A 7 -N 20[20]100`

##### __Non-persistent CSMA/CD simulation is done similarly.__ #####