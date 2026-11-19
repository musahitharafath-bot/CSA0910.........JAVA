#!/bin/sh
# Compile and run Task04
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out SlotDemo | tee output/SlotDemo_output.txt
java -cp out TicketDemo | tee output/TicketDemo_output.txt
