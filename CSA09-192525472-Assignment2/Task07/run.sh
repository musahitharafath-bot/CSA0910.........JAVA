#!/bin/sh
# Compile and run Task07
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
java -cp out TicketRedesign output | tee output/TicketRedesign_output.txt
