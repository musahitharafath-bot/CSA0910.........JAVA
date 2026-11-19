#!/bin/sh
# Compile and run Task02
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out DataTypesDemo | tee output/DataTypesDemo_output.txt
java -cp out JaggedArrayDemo | tee output/JaggedArrayDemo_output.txt
java -cp out InputDemo < input/input.txt | tee output/InputDemo_output.txt
