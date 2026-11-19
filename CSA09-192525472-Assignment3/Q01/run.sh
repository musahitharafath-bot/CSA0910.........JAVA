#!/bin/sh
# Compile and run Q01
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
printf 'ACC1001\n2500\n' | java -cp out Atm | tee output/Atm_output.txt
java -cp out AtmTests | tee output/AtmTests_output.txt
java -cp out UncaughtDemo 2>&1 | tee output/UncaughtDemo_output.txt
javac -d out extras/BadCatchOrder.java 2>&1 | tee output/BadCatchOrder_output.txt
java -cp out ErrorCodeAtm | tee output/ErrorCodeAtm_output.txt
