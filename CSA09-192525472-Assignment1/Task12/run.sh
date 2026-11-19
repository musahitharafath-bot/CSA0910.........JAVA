#!/bin/sh
# Compile and run Task12
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out Task12Demo | tee output/Task12Demo_output.txt
java -cp out VanCases | tee output/VanCases_output.txt
