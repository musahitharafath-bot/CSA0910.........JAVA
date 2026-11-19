#!/bin/sh
# Compile and run Task07
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out OverrideDemo | tee output/OverrideDemo_output.txt
