#!/bin/sh
# Compile and run Task09
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out AbstractDemo | tee output/AbstractDemo_output.txt
java -cp out ChargeableDemo | tee output/ChargeableDemo_output.txt
