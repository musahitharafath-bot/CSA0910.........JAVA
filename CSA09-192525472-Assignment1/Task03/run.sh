#!/bin/sh
# Compile and run Task03
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out ParkingFacility | tee output/ParkingFacility_output.txt
