#!/bin/sh
# Compile and run Q06
cd "$(dirname "$0")"
mkdir -p out/a out/b out/extras output
javac -d out/a src/part_a/*.java
javac -d out/b src/part_b/*.java
javac -d out/extras extras/TokenCounterIf.java
echo "##### Part A: wallet transfers"
java -cp out/a DeadlockDemo     | tee output/DeadlockDemo_output.txt
java -cp out/a OrderedTransfer  | tee output/OrderedTransfer_output.txt
java -cp out/a TryLockTransfer  | tee output/TryLockTransfer_output.txt
echo "##### Part B: token counter (Complete the Code)"
: > output/TokenCounter_output.txt
for i in 1 2 3 4 5 6 7 8 9 10; do printf "run %2d: " $i | tee -a output/TokenCounter_output.txt; java -cp out/b TokenCounter | tee -a output/TokenCounter_output.txt; done
: > output/TokenCounterIf_output.txt
for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do printf "run %2d (if): " $i | tee -a output/TokenCounterIf_output.txt; timeout 5 java -cp out/extras TokenCounterIf 2>&1 | tee -a output/TokenCounterIf_output.txt; [ $? -eq 0 ] || true; done
java -cp out/b TraceDemo        | tee output/TraceDemo_output.txt
java -cp out/b SleepVsWait      | tee output/SleepVsWait_output.txt
java -cp out/b WhileIfExperiment| tee output/WhileIfExperiment_output.txt
java -cp out/b QueueCompare     | tee output/QueueCompare_output.txt
