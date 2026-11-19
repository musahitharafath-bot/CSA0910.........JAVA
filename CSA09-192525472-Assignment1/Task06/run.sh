#!/bin/sh
# Compile and run Task06
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out ConstructorTrace | tee output/ConstructorTrace_output.txt
