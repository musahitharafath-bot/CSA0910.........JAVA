#!/bin/sh
# Compile and run Task02
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
java -cp out PlaylistDemo | tee output/PlaylistDemo_output.txt
java -cp out PlaylistBench output | tee output/PlaylistBench_output.txt
