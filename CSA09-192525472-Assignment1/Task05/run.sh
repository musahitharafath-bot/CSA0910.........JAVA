#!/bin/sh
# Compile and run Task05
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out TicketSequence | tee output/TicketSequence_output.txt
