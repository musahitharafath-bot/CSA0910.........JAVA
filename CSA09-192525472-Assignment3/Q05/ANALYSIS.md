# Q5 – Race Conditions in a Cinema Seat-Booking System (K4 – Analyse)

Programs: `src/unsafe/SeatBooking.java` (TODO 1 without `synchronized`), `src/complete/SeatBooking.java` (TODO 1 with `synchronized`), `Impls.java` (all strategies), `RaceExperiment`, `LockDemo`.

## Complete the Code – results over 10 runs

| Version | 10 runs, 100 threads, 10 seats | Expected |
|---|---|---|
| TODO 1 **without** `synchronized` (`output/SeatBooking_unsafe_10runs.txt`) | `Seats sold: 10` in all 10 runs | 10 |
| TODO 1 **with** `synchronized` (`output/SeatBooking_output.txt`) | `Seats sold: 10` in all 10 runs | 10 |

**Honest observation:** the unsynchronized program is still *wrong*, but it did not fail in these 10 runs. Starting a thread takes much longer than the check-then-act step (a few nanoseconds), so the 100 threads rarely overlap inside the window – bugs of this kind are *latent*. That is why Student A's reproduction test widens the window.

### Why the unsynchronized version may sell more than 10 seats
`book()` is *check-then-act*: `if (!booked[seat]) { booked[seat] = true; bookingsMade++; }`. Two threads can both pass the check before either sets the flag (interleaving below), so one seat is sold twice and the counter exceeds 10. The same kind of problem affects `bookingsMade++` (read-modify-write), which can also lose updates and make the counter *wrong in either direction*.

## Student A – Producing and explaining the race

### Reliable reproduction (`RaceExperiment`, 200 runs of 100 threads on 10 seats; all threads released together by a latch)

| Version | Runs with double booking | Max seats "sold" | Average sold |
|---|---|---|---|
| Unsafe (plain) | 0 / 200 | 10 | 10.00 |
| **Unsafe + `Thread.yield()` between check and act** | **172 / 200** | **36** | **11.32** |
| `synchronized` method | 0 | 10 | 10.00 |
| `synchronized` block | 0 | 10 | 10.00 |
| `ReentrantLock` | 0 | 10 | 10.00 |
| `AtomicBoolean` CAS | 0 | 10 | 10.00 |
| `ConcurrentHashMap.putIfAbsent` | 0 | 10 | 10.00 |

(Earlier 200-run record: 179 / 200, max 41 – the count varies from run to run, which is itself the proof of non-determinism.)
The `yield()` does not *create* the bug; it only makes the harmful interleaving probable enough to test reliably.

### Step-by-step interleaving of two threads (seat 3, both want it)

| step | Thread U1 | Thread U2 | `booked[3]` |
|---|---|---|---|
| 1 | reads `booked[3]` → false (check passes) | | false |
| 2 | *(preempted)* | reads `booked[3]` → false (check passes) | false |
| 3 | | sets `booked[3] = true`, returns "booked" | true |
| 4 | sets `booked[3] = true`, returns "booked" | | true |
| result | **both customers receive seat 3** | | |

### Exact critical section
```java
if (!booked[seat]) {        // read
    booked[seat] = true;    // write that depends on the read
    bookingsMade++;         // shared counter
}
```
These three statements must execute **atomically** with respect to other bookings of the same seat (and of the counter). The fee calculation before it (`Work.fee`) is *not* part of the critical section.

## Student B – Comparing synchronization strategies

### Time (ns per booking; 8 threads; best of 5 repetitions after 2 warm-ups; this sandbox exposes **1 CPU core**)

| Version | 10,000 bookings, 10,000 seats (low contention) | 10,000 bookings, 100 seats (high contention) | 2,000 bookings with a 100 µs I/O wait outside the critical part |
|---|---|---|---|
| `synchronized` method (lock whole method) | 2,065 | 2,198 | **166,719** |
| `synchronized` block (critical section only) | 2,126 | 1,772 | **20,699** |
| `ReentrantLock` | 1,986 | 1,601 | 20,480 |
| `AtomicBoolean` CAS | 1,591 | 1,598 | 20,112 |
| `ConcurrentHashMap` | 1,417 | 1,565 | 20,597 |
| 1 thread, no contention (baseline) | 1,950 | | |

**Reading the numbers.** With only CPU work (and one core) all versions are within ~±25 % – the time is dominated by the 300-iteration fee calculation, and with one core there is no real parallel contention. The experiment that shows the difference is the third column: when part of the method *waits* (a payment gateway call, simulated by 100 µs), locking the **whole method** keeps other threads out during the wait: **166,719 vs 20,699 ns = 8× slower**, essentially fully serialised (≈ 8 threads × 20 µs). Locking only the critical section lets the waits overlap. On a multi-core machine the CPU columns would also separate; the measurements should be repeated on the submission machine.

### Static synchronization – and when it would be needed
`static synchronized` uses the lock of the `Class` object (`SeatBooking.class`), shared by every instance; an instance `synchronized` method uses `this`, which differs for each screen. It is needed here only for state shared by **all screens**, for example a global "tickets sold today" counter. `LockDemo`, 20 screens × 5,000 sales each on a global counter (read-modify-write with a widened window):
```
global ticket counter, expected 100000
  instance-synchronized : 50132      <- screens do not exclude each other: lost updates
  static-synchronized   : 100000
```
For the seat array itself (one per screen object) instance locking is correct and static locking would needlessly serialise all 20 screens.

### The intrinsic lock each version uses (`LockDemo`)
```
synchronized instance method: holds lock(this)=true, holds lock(LockDemo.class)=false
synchronized(this) block      : holds lock(this)=true
static synchronized method  : holds lock(LockDemo.class)=true
```
*Synchronized method* and *`synchronized(this)` block* use the **same** monitor (the screen object); they differ only in how much code is inside. A static method uses the `Class` object's monitor. The monitor is reentrant and is released automatically on exit, even by exception.

## Joint engineering decision – 20 screens
**One `SeatBooking` object per screen** (so 20 independent instance locks and no cross-screen contention) with the lock held **only around the check-then-set** (`synchronized` block, or `AtomicBoolean.compareAndSet` per seat). Reasons from our data: (1) correctness – every variant had 0 double bookings in 200 runs; (2) the large win comes from *lock scope* (8× when waiting happens outside the lock), not from the choice of mechanism (the five mechanisms differ by < 30 % in the CPU-only columns); (3) the `synchronized` block is the simplest to prove correct; switch to per-seat CAS only if profiling shows hot-seat contention. Keep payment/notification calls outside the lock; use `LongAdder` for any global counter instead of a static lock.

## Alternative concept – `AtomicBoolean[]` / `ReentrantLock` / `ConcurrentHashMap` (s seats)
```java
return booked[seat].compareAndSet(false, true);          // AtomicBoolean[]  (one CAS decides the winner)
lock.lock(); try { ... } finally { lock.unlock(); }      // ReentrantLock
return map.putIfAbsent(seat, TRUE) == null;              // ConcurrentHashMap
```

| Approach | Time per booking under contention | Extra space for s seats (typical 64-bit HotSpot, estimates) |
|---|---|---|
| `synchronized` / `ReentrantLock` | O(1) when uncontended; under contention losers **block** (park/unpark, context switches) – cost grows with the number of waiting threads | `boolean[s]` ≈ s bytes + one lock ≈ O(s) with tiny constant (one monitor/lock object) |
| `AtomicBoolean[]` + `compareAndSet` | O(1), **lock-free**: exactly one CAS wins, losers return false immediately – no blocking, no retry loop needed for a one-way flag | s references + s `AtomicBoolean` objects ≈ 16–24 B per seat → O(s) with larger constant |
| `ConcurrentHashMap.putIfAbsent` | O(1) average (hash + CAS on an empty bin, short lock on collision), allocates a node on success | stores only **booked** seats: ≈ 32–48 B per booked seat + table → O(booked) |

Measured (ns per booking, table above): the alternatives were equal or slightly faster than the synchronized versions in the CPU-only columns (1.4–1.6 µs vs 1.8–2.2 µs) and equal to the block version in the I/O column. Because only 1 core was available, the lock-free advantage under real parallel contention is not demonstrated by this data.
