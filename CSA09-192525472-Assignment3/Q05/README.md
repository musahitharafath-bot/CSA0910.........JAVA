# Q05 – Race Conditions in a Cinema Seat-Booking System

**Student A:** Syed Aakif Hussain S A  |  **Register No.:** 192525472  |  **Bloom's level:** K4 – Analyse  |  **Marks:** 6

## Contents

| Folder | What it holds |
|---|---|
| `src/` | Java source code (and `extras/` for variants used in the analysis) |
| `input/` | No input file needed for this task |
| `output/` | Console output (`*_output.txt`) and console screenshots (`*_console.png`) |
| `ANALYSIS.md` | Written answers: Student A / Student B sections, tables, diagrams, complexity analysis |

## Source files

- `src/Booker.java`
- `src/Impls.java`
- `src/LockDemo.java`
- `src/RaceExperiment.java`
- `src/complete/SeatBooking.java`
- `src/unsafe/SeatBooking.java`

## Compile and run

```
sh run.sh
```

or manually from inside this folder (see `run.sh` for the exact commands). Tested with Java 21.0.12.1.

## Result

SeatBooking completed with synchronized (10/10 runs sell exactly 10). The plain unsynchronized version did not fail in 10 runs (latent race); with the check-then-act window widened it double-booked in 172 of 200 runs. synchronized method vs block vs ReentrantLock vs CAS vs ConcurrentHashMap compared; locking the whole method is 8x slower when the method waits on I/O.

## Output

- `output/LockDemo_console.png`
- `output/RaceExperiment_console.png`
- `output/SeatBooking_console.png`

The `.png` files are console-style images rendered from the real program output. For submission, replace them with your own screenshots taken on your computer.
