# Q06 – Deadlock and Inter-Thread Communication

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 7

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | No input file needed for this task |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `extras/TokenCounterIf.java`
- `src/part_a/Account.java`
- `src/part_a/DeadlockDemo.java`
- `src/part_a/OrderedTransfer.java`
- `src/part_a/TryLockTransfer.java`
- `src/part_b/Buf.java`
- `src/part_b/QueueCompare.java`
- `src/part_b/SleepVsWait.java`
- `src/part_b/TokenCounter.java`
- `src/part_b/TraceDemo.java`
- `src/part_b/WhileIfExperiment.java`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

TokenCounter completed with wait()/notifyAll() in while loops: sum 210 in 10/10 runs. Replacing while with if gives wrong sums in 12 of 20 runs. notify() deadlocks 40/40 trials with 2 producers + 2 consumers. A reliable wallet-transfer deadlock was built, detected by the JVM and removed by lock ordering; tryLock and ArrayBlockingQueue alternatives measured.

## Output

- `output/DeadlockDemo_console.png`
- `output/OrderedTransfer_console.png`
- `output/QueueCompare_console.png`
- `output/SleepVsWait_console.png`
- `output/TokenCounterIf_console.png`
- `output/TokenCounter_console.png`
- `output/TraceDemo_console.png`
- `output/TryLockTransfer_console.png`
- `output/WhileIfExperiment_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
