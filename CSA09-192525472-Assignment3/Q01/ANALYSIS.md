# Q1 – Handling Failure in a Bank ATM Simulation (K4 – Analyse)

All numbers below come from the programs in `src/` (outputs in `output/`).

## Shared work – every point where a withdrawal can fail

| # | Failure point | Exception | Category | Caught in `Atm.run()` by |
|---|---|---|---|---|
| FAIL-1 | card-reader file missing | `FileNotFoundException` | **checked** (subclass of `IOException`) | `catch (FileNotFoundException)` |
| FAIL-1b | other read error on the file | `IOException` | **checked** | `catch (IOException)` |
| FAIL-2 | account number not on the card file | `IllegalStateException` | **unchecked** | `catch (RuntimeException)` (safety net) |
| FAIL-3 | letters / empty text typed as amount | `NumberFormatException` | **unchecked** | multi-catch `NumberFormatException \| ArrayIndexOutOfBoundsException` |
| FAIL-3b | malformed record in the card file (missing field) | `ArrayIndexOutOfBoundsException` | **unchecked** | same multi-catch |
| FAIL-4 | negative or zero amount | `IllegalArgumentException` | **unchecked** | `catch (IllegalArgumentException)` |
| FAIL-5 | amount larger than balance | `InsufficientFundsException` (our class) | **checked** (extends `Exception`) | `catch (InsufficientFundsException)` |
| – | JVM runs out of memory / stack | `OutOfMemoryError`, `StackOverflowError` | **Error** | deliberately **not** caught – the program cannot recover |

Why these categories: a missing file and a too-low balance are *expected, recoverable* situations that the caller must handle, so they are checked (the compiler forces the decision). A wrong argument or a bad number is a mistake in the data/program, so it is unchecked. Errors signal a broken JVM and are not handled.

## Student A – Exception design

### Throwable hierarchy relevant to the ATM

```
Throwable
├── Error                                  (not caught)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── IOException                        CHECKED
    │   └── FileNotFoundException          CHECKED  (FAIL-1)
    ├── InsufficientFundsException         CHECKED  (our class, FAIL-5)
    └── RuntimeException                   UNCHECKED
        ├── IllegalStateException          (FAIL-2)
        ├── IllegalArgumentException       (FAIL-4)
        │   └── NumberFormatException      (FAIL-3)
        └── IndexOutOfBoundsException
            └── ArrayIndexOutOfBoundsException (FAIL-3b)
```

### Justification of the catch-block order (in `Atm.run`)

1. `FileNotFoundException` **before** `IOException` – it is a subclass; if `IOException` came first the narrower block could never run.
2. `NumberFormatException | ArrayIndexOutOfBoundsException` **before** `IllegalArgumentException` – `NumberFormatException` *is an* `IllegalArgumentException`. (A multi-catch cannot list both: javac says "Alternatives in a multi-catch statement cannot be related by subclassing" – we hit this error while writing the program, so the multi-catch only lists *unrelated* types.)
3. `IllegalArgumentException`, then `InsufficientFundsException` (unrelated to the others, so any position is legal).
4. `RuntimeException` **last** – it is the parent of every unchecked type above, so it acts only as a safety net for unknown unchecked failures such as FAIL-2.

Rule: **most specific first, most general last.** Messages are then specific ("card reader file missing" instead of "something failed").

### What `finally` guarantees here

The `finally` block (prints "card returned, session closed") runs **once per withdrawal on every path**: normal completion, after any catch block, and even when an exception is *not* caught by this method (it runs and the exception then continues upwards). The test table confirms it ran for all 7 inputs. It does *not* run if `System.exit()` is called, the JVM crashes, or the thread loops forever – so it is the right place to release the card/session, but cannot be a substitute for process-level safety.

## Student B – Testing and tracing

### Test table (`AtmTests`, output in `output/AtmTests_output.txt`)

| Test | Input (account, amount) | Path taken | finally ran? |
|---|---|---|---|
| T1 | ACC1001, 1500 | normal – balance left 3500 | yes |
| T2 | ACC1001, `abc` | multi-catch (`NumberFormatException`: `For input string: "abc"`) | yes |
| T3 | ACC1001, -300 | `IllegalArgumentException: Amount must be positive` | yes |
| T4 | ACC1002, 900 | `InsufficientFundsException: Balance 200 < requested 900` | yes |
| T5 | file `input/nofile.txt` | `FileNotFoundException` → "card reader file missing" | yes |
| T6 | ACC9999, 100 | `RuntimeException` safety net (`IllegalStateException: Unknown account`) | yes |
| T7 | ACC1001, *(empty)* | multi-catch (`NumberFormatException: For input string: ""`) | yes |

Seven inputs trigger six different catch blocks (T2 and T7 share one).

### Stack trace of one uncaught exception (`UncaughtDemo`)

```
Exception in thread "main" java.lang.NumberFormatException: For input string: "12x"
	at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
	at java.base/java.lang.Integer.parseInt(Integer.java:662)
	at java.base/java.lang.Integer.parseInt(Integer.java:778)
	at UncaughtDemo.parseAmount(UncaughtDemo.java:3)
	at UncaughtDemo.withdraw(UncaughtDemo.java:4)
	at UncaughtDemo.main(UncaughtDemo.java:7)
```

* Line 1 – the thread (`main`), the exception class, and its message (the bad text).
* Lines 2-4 – frames **inside the JDK**: where the error was detected (`forInputString`) and the two `parseInt` overloads. Read from top (where it was thrown) to bottom (who called whom).
* Line 5 – **our** code that called `parseInt` (`parseAmount`, line 3).
* Line 6 – `withdraw` called `parseAmount`.
* Line 7 – `main` called `withdraw` at line 7 and nothing caught the exception, so the thread died; "After (never printed)" was not printed.

### Broader catch before narrower (`extras/BadCatchOrder.java`)

`javac` refuses to compile it:

```
extras/BadCatchOrder.java:6: error: exception NumberFormatException has already been caught
        catch (NumberFormatException e) { System.out.println("narrow"); }  // unreachable
```
The compiler detects that the narrower block is unreachable. (Evidence: `output/BadCatchOrder_output.txt`.)

## Joint design question – should the ATM use one `catch (Exception e)`?

Acceptable only at the **outermost boundary** (e.g. the top of the ATM's request loop) to log the failure, show a generic "Service unavailable", and keep the machine running – *after* the specific cases have been handled. Used as the only handler it loses: (1) different recovery per failure (re-prompt vs. "no funds" vs. "call the bank"); (2) specific, user-friendly messages; (3) the checked-exception contract – real bugs such as `NullPointerException` get swallowed as if they were normal user mistakes; (4) it also catches `InterruptedException`, hiding thread-interruption requests. Both students can defend this: catch what you can *handle*, let the rest propagate.

## Alternative concept – error codes instead of exceptions (`ErrorCodeAtm.java`)

```java
static int withdraw(String text, int balance) {
    if (text == null || !text.matches("-?\\d+")) return -1;   // check 1: format
    int amount = Integer.parseInt(text);
    if (amount <= 0)       return -2;                          // check 2: sign
    if (amount > balance)  return -3;                          // check 3: funds
    return balance - amount;
}
```
(or `Scanner.hasNextInt()` before `nextInt()`). Every caller must test the return value; a forgotten test silently treats `-3` as a balance.

| | Exceptions | Error codes |
|---|---|---|
| Readability | happy path is a straight line; failures are in `catch` blocks | checks and `if (code < 0)` repeated at *every* call level; failure and result share one `int` |
| Number of checks | 0 explicit pre-checks for format (parseInt validates); 2 `if`s | 3 pre-checks per withdrawal **plus** a code test in each caller |
| Forced handling | checked types force the caller | nothing forces the caller |
| **Time** | creating/throwing = `fillInStackTrace` = **O(d)** for stack depth d, plus unwinding | **O(1)** per failure (return an int) |
| **Space** | exception object + stack trace array **O(d)** (see Q2: ~1.2 KB at depth 5, ~39 KB at depth 500) | **O(1)** – one `int` |

Measured (`ErrorCodeAtm`, 200,000 iterations, ns per call, shallow stack):

| input | error-code version | exception version |
|---|---|---|
| valid ("500") | 72 ns | 3 ns |
| invalid ("abc") | 74 ns | 311 ns |

Reading: on a **failing** input the exception version is ~4× slower (311 vs 74 ns) because of the stack-trace capture. On the **happy path** exceptions cost nothing (3 ns), while my error-code version pays for a regex pre-check on every call (72 ns). So: use exceptions for *rare* failures (ATM input errors are rare), error codes/pre-checks only when failures are frequent and hot.
