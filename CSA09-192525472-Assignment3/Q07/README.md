# Q07 – Design a Concurrent, Fault-Tolerant Processor from First Principles

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K5 – Evaluate  |  **Marks:** 10

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | Input files used by the programs |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `src/Alternatives.java`
- `src/Benchmark.java`
- `src/FileOutcome.java`
- `src/GenData.java`
- `src/ResultProcessor.java`
- `src/ScoreFormatException.java`
- `src/ScoreParser.java`
- `src/ScoreTask.java`
- `src/ScoreValidationException.java`
- `src/SmallCheck.java`
- `src/Status.java`
- `src/Summary.java`
- `src/Verify.java`

## Input files

- `input/small/bad_letters.txt`
- `input/small/empty.txt`
- `input/small/invalid_range.txt`
- `input/small/manifest.txt`
- `input/small/ok1.txt`
- `input/small/ok2.txt`
- `input/small/ok3.txt`
- `input/small/ok4.txt`
- `input/small/ok5.txt`
- `input/small/poison_bug.txt`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

ExecutorService + Callable + Future processor with a ConcurrentHashMap result set. 10,000 generated files with 384 injected faults: summary counts and the sum of averages match the independent expectation exactly; 10 hand-checked files pass. Speed-up with 1 ms storage latency is 4.0x (4 threads) and 8.0x (8 threads); without latency it flattens at about 2x. Parallel streams abort on one unhandled exception; one thread per file does not scale in memory.

## Output

- `output/Alternatives_console.png`
- `output/Benchmark_console.png`
- `output/GenData_console.png`
- `output/SmallCheck_console.png`
- `output/Verify_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
