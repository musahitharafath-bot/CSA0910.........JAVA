#!/bin/sh
# Compile and run Task10
cd "$(dirname "$0")"
mkdir -p out output
javac -d out $(find src -name "*.java")
java -cp out parking.app.Main input/events.txt | tee output/Main_output.txt
mkdir -p out_extras
javac -d out_extras extras/*.java
java -cp out_extras WrapperDemo | tee output/WrapperDemo_output.txt
java -cp out_extras FacilityConfig | tee output/FacilityConfig_output.txt
