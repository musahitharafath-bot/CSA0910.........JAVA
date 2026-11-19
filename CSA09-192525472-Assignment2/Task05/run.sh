#!/bin/sh
# Compile and run Task05
cd "$(dirname "$0")"
mkdir -p out out_before output
javac -d out src/*.java
java -cp out LeaderboardDemo | tee output/LeaderboardDemo_output.txt
java -cp out RankingDesign | tee output/RankingDesign_output.txt
java -cp out RankBench output | tee output/RankBench_output.txt
