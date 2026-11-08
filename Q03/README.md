# Q03 – Resource Safety, Debugging and Robust Code

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 7

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | Input files used by the programs |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `src/ReportGenerator.java`
- `src/ScaleDemo.java`
- `src/StoreFileException.java`
- `src/SuppressedDemo.java`
- `src/TrackedReader.java`
- `src/TrackedWriter.java`

## Input files

- `input/store1.csv`
- `input/store2.csv`
- `input/store3_corrupt.csv`
- `input/store3_fixed.csv`
- `input/store4.csv`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

Four report generators: manual close without finally (leaks 2 handles, report 0 bytes on failure), try-finally (0 leaks), try-with-resources (0 leaks), and no close (leaks f+1 handles; 'Too many open files' reproduced after 433 files). V1 and V2 produce identical reports. Closing order and suppressed exceptions demonstrated.

## Output

- `output/ReportGenerator_console.png`
- `output/ScaleDemo_console.png`
- `output/SuppressedDemo_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer (and add IDE debugger screenshots for the breakpoint / step-through part of Student A's work).
