#!/bin/sh
# Compile and run Q02
cd "$(dirname "$0")"
mkdir -p out/complete out/portal out/extras output
javac -d out/complete src/complete/*.java
javac -d out/portal src/portal/*.java
javac -d out/extras extras/NoCauseDemo.java
java -cp out/complete StorageDemo | tee output/StorageDemo_output.txt
java -cp out/extras NoCauseDemo | tee output/NoCauseDemo_output.txt
java -cp out/portal PortalDemo | tee output/PortalDemo_output.txt
java -cp out/portal PropagationDemo | tee output/PropagationDemo_output.txt
java -cp out/portal ChainDemo | tee output/ChainDemo_output.txt
java -cp out/portal ExceptionCost | tee output/ExceptionCost_output.txt
