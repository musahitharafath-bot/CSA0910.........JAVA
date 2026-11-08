#!/bin/sh
# Compile and run Q04
cd "$(dirname "$0")"
mkdir -p out output
javac -d out src/*.java
java -cp out CreationDemo   | tee output/CreationDemo_output.txt
java -cp out RunVsStart     | tee output/RunVsStart_output.txt
java -cp out LifeCycle      | tee output/LifeCycle_output.txt
java -cp out OrderExperiment| tee output/OrderExperiment_output.txt
java -cp out DaemonDemo user   | tee output/DaemonDemo_output.txt
java -cp out DaemonDemo daemon | tee -a output/DaemonDemo_output.txt
java -cp out PoolDemo       | tee output/PoolDemo_output.txt
