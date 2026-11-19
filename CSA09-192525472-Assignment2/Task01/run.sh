#!/bin/sh
# Compile and run Task01
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
java -cp out TicketBench output | tee output/TicketBench_output.txt
java -cp out SharedLogDemo output | tee output/SharedLogDemo_output.txt
