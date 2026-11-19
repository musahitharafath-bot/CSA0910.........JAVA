# Q04 – Thread Creation and the Thread Life Cycle

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 7

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | No input file needed for this task |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `src/CreationDemo.java`
- `src/DaemonDemo.java`
- `src/LifeCycle.java`
- `src/OrderExperiment.java`
- `src/PoolDemo.java`
- `src/RunVsStart.java`
- `src/StationTask.java`
- `src/StationThread.java`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

Four stations implemented with extends Thread and with Runnable (total 130 scripts both ways). run() vs start(), all thread states, 12-run order experiments (8 and 11 distinct orders), priorities (no reliable effect), daemon vs user thread, and a fixed-thread-pool alternative whose time matches ceil(batches/p) x batch time.

## Output

- `output/CreationDemo_console.png`
- `output/DaemonDemo_console.png`
- `output/LifeCycle_console.png`
- `output/OrderExperiment_console.png`
- `output/PoolDemo_console.png`
- `output/RunVsStart_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
