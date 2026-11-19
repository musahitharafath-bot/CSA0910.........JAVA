#!/bin/sh
# Compile and run Q05
cd "$(dirname "$0")"
mkdir -p out/complete out/unsafe out/exp output
javac -d out/unsafe   src/unsafe/SeatBooking.java
javac -d out/complete src/complete/SeatBooking.java
javac -d out/exp src/Booker.java src/Impls.java src/RaceExperiment.java src/LockDemo.java
: > output/SeatBooking_unsafe_10runs.txt
for i in 1 2 3 4 5 6 7 8 9 10; do printf "run %2d: " $i | tee -a output/SeatBooking_unsafe_10runs.txt; java -cp out/unsafe SeatBooking | tee -a output/SeatBooking_unsafe_10runs.txt; done
: > output/SeatBooking_output.txt
for i in 1 2 3 4 5 6 7 8 9 10; do printf "run %2d: " $i | tee -a output/SeatBooking_output.txt; java -cp out/complete SeatBooking | tee -a output/SeatBooking_output.txt; done
java -cp out/exp LockDemo | tee output/LockDemo_output.txt
java -cp out/exp RaceExperiment | tee output/RaceExperiment_output.txt
