#!/bin/sh
# Compile and run Task06
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
java -cp out Triage | tee output/Triage_output.txt
java -cp out TriageDesign | tee output/TriageDesign_output.txt
java -cp out TriageBench output | tee output/TriageBench_output.txt
