#!/bin/sh
# Compile and run Task08
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out FeeDemo | tee output/FeeDemo_output.txt
java -cp out OverloadDemo | tee output/OverloadDemo_output.txt
