#!/bin/sh
# Compile and run Q03
cd "$(dirname "$0")"
mkdir -p out output
rm -f output/report.log output/report.log.lck
javac -d out src/*.java
java -cp out ReportGenerator | tee output/ReportGenerator_output.txt
java -cp out SuppressedDemo | tee output/SuppressedDemo_output.txt
# lower the open-file limit so the leak shows up quickly
( ulimit -n 256; java -cp out ScaleDemo 1000 | tee output/ScaleDemo_output.txt )
