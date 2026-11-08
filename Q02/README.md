# Q02 – User-Defined Exceptions and Propagation in an Online Examination Portal

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 7

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | No input file needed for this task |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `extras/NoCauseDemo.java`
- `src/complete/StorageDemo.java`
- `src/portal/ChainDemo.java`
- `src/portal/ExamController.java`
- `src/portal/ExamException.java`
- `src/portal/ExamWindowClosedException.java`
- `src/portal/ExceptionCost.java`
- `src/portal/InvalidSubmissionException.java`
- `src/portal/NotRegisteredException.java`
- `src/portal/PortalDemo.java`
- `src/portal/PropagationDemo.java`
- `src/portal/Storage.java`
- `src/portal/SubmissionService.java`
- `src/portal/SubmissionStorageException.java`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

ExamException hierarchy (3 checked subclasses + 1 unchecked) across controller / service / storage. StorageDemo (TODO 1 completed) prints exactly the expected output. Chaining keeps the IOException as the cause; without it getCause() is null and the Cause line throws NullPointerException. Stack-trace capture cost is O(depth): ~2 us / 1.2 KB at depth 5, ~63 us / 39 KB at depth 500.

## Output

- `output/ChainDemo_console.png`
- `output/ExceptionCost_console.png`
- `output/NoCauseDemo_console.png`
- `output/PortalDemo_console.png`
- `output/PropagationDemo_console.png`
- `output/StorageDemo_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
