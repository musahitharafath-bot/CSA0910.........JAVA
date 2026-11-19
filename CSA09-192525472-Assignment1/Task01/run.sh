#!/bin/sh
# Compile and run Task01
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out Task01Demo | tee output/Task01Demo_output.txt
