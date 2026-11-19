# CSA09-192525472-Assignment3

**Exception Handling and Multithreading – Bank ATM, Examination Portal, Sales Reports, Scanner Stations, Cinema Booking, Hostel Wallet / Mess Counter, Result Processing**

Student A: Syed Aakif Hussain S A  |  Register Number: 192525472  |  Course: Programming in Java (CO3)  |  SIMATS Engineering
Student B: *(partner name and register number)*

Compiled and run with Java 21.0.12.1

## Folder layout

Each question has its own folder with the same sub-folders:

```
QNN/
  src/       Java source code
  extras/    variants used for experiments (where needed)
  input/     input files (where the question uses any)
  output/    program output (.txt) and console screenshots (.png)
  ANALYSIS.md  written answers (Student A / Student B sections, tables, complexity analysis)
  README.md  what the question contains and how to run it
  run.sh     compile and run commands
```

| Folder | Question | Marks | Main source files |
|---|---|---|---|
| [Q01](Q01/) | Handling Failure in a Bank ATM Simulation | 6 | Atm.java, AtmTests.java, UncaughtDemo.java, ErrorCodeAtm.java |
| [Q02](Q02/) | User-Defined Exceptions and Propagation in an Online Examination Portal | 7 | complete/StorageDemo.java, portal/*.java |
| [Q03](Q03/) | Resource Safety, Debugging and Robust Code | 7 | ReportGenerator.java, SuppressedDemo.java, ScaleDemo.java |
| [Q04](Q04/) | Thread Creation and the Thread Life Cycle | 7 | StationThread.java, StationTask.java, LifeCycle.java, OrderExperiment.java, PoolDemo.java |
| [Q05](Q05/) | Race Conditions in a Cinema Seat-Booking System | 6 | complete/SeatBooking.java, unsafe/SeatBooking.java, RaceExperiment.java |
| [Q06](Q06/) | Deadlock and Inter-Thread Communication | 7 | part_a/*.java, part_b/TokenCounter.java, part_b/*.java |
| [Q07](Q07/) | Concurrent, Fault-Tolerant Processor | 10 | ResultProcessor.java, ScoreTask.java, Benchmark.java |

## Running

Requires JDK 17 or later (records and `Thread.threadId()` need 17 / 19+; tested on 21).

```
cd Q05
sh run.sh
```

Q07 first generates the 10,000-file data set into `Q07/data/` (not committed; see `.gitignore`).

## Individual Contribution Record

Fill in your own names; the split alternates as the assignment requires and *both* students executed every program.

| Question | Student A – primary responsibility | Student B – primary responsibility | Joint |
|---|---|---|---|
| Q1 | Exception design (hierarchy, table, catch order, finally) | Testing and tracing (5+ tests, stack trace, broad-catch demo) | Failure points, implementation, design question |
| Q2 | Hierarchy diagram, checked/unchecked reasons, propagation path | Failure scenarios, chaining evidence, test table | Hierarchy design, throw/throws, chaining, conclusion |
| Q3 | Debugging, failing input, handle counts, logging | Resource analysis, try-with-resources order, suppressed exceptions | Two implementations, engineering decision |
| Q4 | Thread design, life-cycle diagram, run() vs start(), join() | 10+ runs, priorities, daemon thread, scheduler explanation | Both creation styles, conclusion |
| Q5 | Reproduce and explain the race, critical section | Timing comparison, static sync, intrinsic locks | Three versions, deployment decision |
| Q6 | Producer–consumer design (wait loop, notify, trace, sleep vs wait) | Deadlock construction, four conditions, lock ordering | Both parts, conclusion |
| Q7 | Stage 2 – design (executor, collection, exceptions, diagram) | Stage 3 – implement and measure | Stage 1 – interpret, Stage 4 – defend |

## Suggested commit history

One commit per question, for example: `Q01: ATM exception handling`, `Q02: exam portal exception hierarchy`, … `Q07: concurrent fault-tolerant processor`. Commit progressively; a single upload on the last day is not accepted.

## Notes on the measurements

All timings in `ANALYSIS.md` were taken in a sandbox that exposes **one CPU core**; re-run the experiments (`sh run.sh`) on your own machine and replace the screenshots and tables with your own numbers. Race-condition and thread-order results vary from run to run by nature.
