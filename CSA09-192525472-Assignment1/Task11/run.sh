#!/bin/sh
# Compile and run Task11
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out FeeDemo | tee output/FeeDemo_output.txt
java -cp out ParkingFacility | tee output/ParkingFacility_output.txt
java -cp out ParkingTests | tee output/ParkingTests_output.txt
java -cp out DailyCollection | tee output/DailyCollection_output.txt
