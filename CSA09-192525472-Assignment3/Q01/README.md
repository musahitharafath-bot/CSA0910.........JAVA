# Q01 – Handling Failure in a Bank ATM Simulation

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 6

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | Input files used by the programs |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `extras/BadCatchOrder.java`
- `src/Atm.java`
- `src/AtmTests.java`
- `src/ErrorCodeAtm.java`
- `src/InsufficientFundsException.java`
- `src/UncaughtDemo.java`

## Input files

- `input/cardreader.txt`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

The ATM withdrawal uses try / multi-catch / finally. Five failure points were classified (checked / unchecked / Error). The 7 test inputs reach 6 different catch blocks and the finally block ran in every case. A catch block placed before a narrower one is rejected by javac. On a failing input, creating an exception is about 4x slower than returning an error code (311 ns vs 74 ns); on the happy path it costs nothing.

## Output

- `output/AtmTests_console.png`
- `output/Atm_console.png`
- `output/BadCatchOrder_console.png`
- `output/ErrorCodeAtm_console.png`
- `output/UncaughtDemo_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
