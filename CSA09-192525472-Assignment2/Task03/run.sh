#!/bin/sh
# Compile and run Task03
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
javac -d out_before before/Attendance.java
java -cp out_before Attendance | tee output/Attendance_before_output.txt
java -cp out Attendance | tee output/Attendance_output.txt
java -cp out ContractExperiments | tee output/ContractExperiments_output.txt
java -cp out DedupBench output | tee output/DedupBench_output.txt
