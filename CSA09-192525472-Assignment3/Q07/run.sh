#!/bin/sh
# Compile and run Q07  (generates the 10,000-file data set under ./data first; data/ is not committed)
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out GenData data 10000      | tee output/GenData_output.txt
java -cp out SmallCheck              | tee output/SmallCheck_output.txt
java -cp out Verify 4                | tee output/Verify_output.txt
java -cp out Benchmark               | tee output/Benchmark_output.txt
java -cp out Alternatives            | tee output/Alternatives_output.txt
