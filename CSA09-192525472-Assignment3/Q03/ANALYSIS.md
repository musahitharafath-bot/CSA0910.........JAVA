# Q3 – Resource Safety, Debugging and Robust Code (K4 – Analyse)

Four versions of the daily sales report generator were implemented in `src/ReportGenerator.java`:

| Version | Method | Purpose |
|---|---|---|
| V0 | manual `close()` **not** in `finally` | the buggy "before" version that leaks |
| V1 | `try-catch-finally` with manual `close()` (each close guarded) | correct manual version |
| V2 | `try-with-resources` | recommended version |
| V3 | no closing at all | worst case, for the alternative analysis |

`TrackedReader` / `TrackedWriter` count how many streams are currently open, so "file handles left open" is measured, not guessed.
Input: `input/store1.csv`, `store2.csv`, `store3_fixed.csv`, `store4.csv` (good) and `store3_corrupt.csv` (line 2 is `book,abc,150.50`).

## Shared work – both versions give the same report (`ReportGenerator_output.txt`)

```
V1 report identical to V2 report: true
input/store1.csv total=1150.00
input/store2.csv total=160.00
input/store3_fixed.csv total=240.00
input/store4.csv total=1633.00
GRAND TOTAL=3183.00
```
(Hand check: store1 = 10×5 + 2×150.50 + 799 = 1150; store2 = 100 + 60 = 160; store3_fixed = 6×40 = 240; store4 = 35 + 1598 = 1633; total 3183.)

## Student A – Debugging and experimental analysis

### Where the manual version leaks
In `v0Naive`, inside the loop:
```java
TrackedReader r = new TrackedReader(f);
double t = storeTotal(r, f);     // <- throws StoreFileException on the corrupt record
r.close();                       // <- never reached: the reader stays open
```
and after the loop `w.close()` is also skipped, so the **writer stays open and its buffered text is never flushed**.

**Debugger procedure (to be repeated in your IDE and screenshotted):** put a breakpoint on `double t = storeTotal(r, f);` in `v0Naive`, run with `bad` input, *Step Into* `storeTotal`; at the 2nd line of `store3_corrupt.csv` the `catch (RuntimeException e)` runs and execution jumps out of both methods; in the *Variables* window `TrackedReader.open == 2` and `openNames == [output/report_v0_bad.txt, input/store3_corrupt.csv]`; the line `r.close()` is never highlighted. (A command-line equivalent is printed by `ReportGenerator`: the "open handles left" column.)

### Failing input that makes the manual version fail part-way
`store3_corrupt.csv` (third of four files): store1 and store2 are fine, store3 fails on line 2, store4 is never reached.

### Files left open for the same failing input

| Version | Result | Open handles left | Report bytes on disk |
|---|---|---|---|
| V0 manual, no finally | FAILED on store3_corrupt | **2** (store3 reader + report writer) | **0** (buffer never flushed – "incomplete report") |
| V1 manual + finally | FAILED on store3_corrupt | **0** | 61 (store1 + store2 lines) |
| V2 try-with-resources | FAILED on store3_corrupt | **0** | 61 |
| V3 no close | FAILED on store3_corrupt | **4** (writer, store1, store2, store3) | 0 |

On the *good* input V0, V1, V2 all end with 0 open; V3 leaves **5** open (4 readers + writer) even though nothing failed.

### "Too many open files" reproduced (`ScaleDemo`, 1,000 store files)
```
V2 try-with-resources : 1000 files done, max handles open at once = 2, open at end = 0
V3 no close           : FAILED after 433 handles -> .../s432.csv (Too many open files)
```
(The exact number depends on the machine's `ulimit -n`; here it was 433.) This is exactly the production symptom described in the question.

### Logging of the failing file (`output/report.log`)
```
SEVERE file=input/store3_corrupt.csv line=2 text='book,abc,150.50' cause=java.lang.NumberFormatException: For input string: "abc"
```
The log line is written at the point of failure in `storeTotal`; the `StoreFileException` also carries `fileName`, which `attempt()` prints as "FAILED on input/store3_corrupt.csv".

## Student B – Design and correctness

### Resources opened and where they must be closed

| Resource | Opened | Must be closed |
|---|---|---|
| report writer (`TrackedWriter`) | once, before the loop | after the last store line is written **or** when any exception occurs (also flushes the buffer) |
| store reader (`TrackedReader`) × f | one per store file, inside the loop | as soon as that store is finished – before the next one is opened |

### Why the manual version can leak
`close()` is an ordinary statement on the normal path. When `storeTotal` throws, control leaves the method through the exception and the statements after it (`r.close()`, `w.close()`) are skipped. Only a `finally` block (or try-with-resources) runs on *both* paths.

### How try-with-resources guarantees closing, including order
The compiler expands `try (R r = …) { body }` into a `try/finally` that calls `r.close()` whether the body completes or throws. With several resources they are closed in **reverse order of declaration** – evidence from `SuppressedDemo`:
```
open  reader
open  writer
body runs
close writer      <- last opened, first closed
close reader
```
Each resource that was *successfully opened* is closed even if a later resource's constructor fails.

### Suppressed exceptions
If the body throws **and** `close()` also throws, the body's exception stays *primary* and the close failures are attached to it as *suppressed* (`getSuppressed()`); nothing is lost, and the real cause is not hidden by a secondary one. Evidence:
```
PRIMARY   : java.lang.IllegalStateException: body failed
SUPPRESSED: java.lang.Exception: close() failed for writer
SUPPRESSED: java.lang.Exception: close() failed for reader
-- body fine, close fails: it becomes the primary exception --
PRIMARY   : java.lang.Exception: close() failed for only  suppressed=0
```
In a hand-written `finally`, an exception thrown by `close()` would **replace** the body's exception and the original would be lost (that is why V1 wraps each close in its own try/catch).

## Joint engineering decision – when is try-with-resources *not* enough?
1. **Close can fail**: a failed `close()` on the *writer* may mean the last buffer was never written – the report is incomplete even though no leak occurs; it must be reported, not ignored.
2. **Policy, not resources**: one corrupt store file still aborts the whole report (our V2 stops at store3). A robust generator also needs per-file catch-and-continue, or validation, and a summary of the failures.
3. **Partial output**: closing guarantees no leak but not an *atomic* report; write to a temporary file and rename on success.
4. **Wrapped constructors**: `try (BufferedReader r = new BufferedReader(new FileReader(f)))` leaks the `FileReader` if the `BufferedReader` constructor throws – declare both as separate resources.
5. **Many resources at once**: opening all f readers up-front still holds f handles, however they are closed.
6. **Non-`AutoCloseable` resources** (locks, temp files, DB transactions) still need `finally`.

## Alternative concept – explicit `close()` in `finally`, or no closing at all (f store files, L total lines)

| | try-with-resources (V2) | `close()` in `finally` (V1) | no closing (V3) |
|---|---|---|---|
| **Time** | O(L) (read every line once) + O(f) opens/closes | O(L + f) | O(L + f) – close cost skipped, but descriptors are released only by GC/finalisation |
| **Max handles open at any time** (sequential processing) | **2** (writer + 1 reader) | **2** | **f + 1** on success – grows with every file (measured: 5 for f = 4; failed at ~433 in `ScaleDemo`) |
| **Handles left open when failure occurs at file k** | **0** after unwinding | **0** | **k + 1** (k readers incl. the failing one + writer); measured 4 for k = 3 |
| Report written? | partial report flushed | partial report flushed | buffer never flushed → 0 bytes |
| Code size / risk | smallest, hardest to get wrong | long; easy to forget a null check or let `close()` mask the real exception | trivial but incorrect |

Space: the *memory* of the program is O(1) for V1/V2 apart from the line buffer; only the descriptor count (an OS resource, limited by `ulimit -n`) differs: **O(1)** for V1/V2 against **O(f)** for V3.
