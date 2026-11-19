# Q6 – Deadlock and Inter-Thread Communication (K4 – Analyse)

Part A (wallet transfers): `src/part_a/` – `DeadlockDemo`, `OrderedTransfer`, `TryLockTransfer`.
Part B (mess token counter): `src/part_b/` – `TokenCounter` (Complete the Code), `Buf`, `TraceDemo`, `WhileIfExperiment`, `SleepVsWait`, `QueueCompare`; `extras/TokenCounterIf.java`.

## Complete the Code – `TokenCounter` (TODO 1 and TODO 2)
```java
put:  while (count == buffer.length) wait();  buffer[in] = token; in = (in + 1) % buffer.length; count++; notifyAll();
take: while (count == 0) wait();  int t = buffer[out]; out = (out + 1) % buffer.length; count--; notifyAll(); return t;
```
10 runs (`output/TokenCounter_output.txt`): `Sum of tokens collected: 210 (expected 210)` – all 10 runs correct (1+…+20 = 210).

### Change `while` to `if` (`extras/TokenCounterIf.java`, 20 JVM runs, `output/TokenCounterIf_output.txt`)
Sums seen: 145, 135, 210, 210, 145, 175, 175, 210, 145, 135, 60, 175, 210, 120, 175, 210, 210, 210, 210, 145 → **12 of 20 runs wrong, 8 correct**. In-process (`WhileIfExperiment`, 300 trials): `while` **300/300 correct**; `if` **205/300 correct, 95 wrong, counter went negative in 95 trials**.
**Explanation:** with `notifyAll()` both students wake up. One gets the monitor first and takes the only token; the other then re-acquires the monitor and, because `if` does not re-test, continues as if a token were present: it takes a *stale* slot (a value that was already collected) and `count` becomes −1. Tokens are collected twice or skipped, so the sum is wrong. `while` re-tests the condition after every wake-up, so the second student simply goes back to waiting.

## Student A – Producer–consumer design

### Why `wait()` must be in a loop and in a `synchronized` block
* **Synchronized:** `wait()` atomically releases the monitor and sleeps; calling it without owning the monitor throws `IllegalMonitorStateException`. Holding the monitor also makes "test condition, then wait" atomic, so a `notify` cannot slip in between the test and the wait (no lost wake-up).
* **Loop:** (1) between being notified and re-acquiring the lock another thread may have changed the state (shown by the `if` experiment above); (2) spurious wake-ups are allowed by the JVM spec; (3) `notifyAll()` wakes threads for which the condition may not hold.

### `notify()` instead of `notifyAll()`
* In the **assigned design** (1 cook, 2 students, capacity 5) `notify()` happens to work: the cook waits only when the counter is full and a student only when it is empty – these never hold at once, so the wait set never contains both kinds, and any thread woken can make progress (a student woken by another student simply re-waits and the next `put` wakes it again).
* In the **general case** it goes wrong. `WhileIfExperiment`, capacity 1, 2 producers + 2 consumers, 20 items each, 40 trials:
```
notifyAll() : 40 of 40 trials completed, 0 deadlocked
notify()    :  0 of 40 trials completed, 40 deadlocked
```
A separate check with a 4-second wait shows `P0 WAITING, P1 WAITING, C2 WAITING, C3 WAITING` – a real deadlock: a consumer's `notify()` woke another consumer instead of the waiting producer, so the wake-up was "lost".

### Counter size over time (`TraceDemo`, size after each of the 40 operations)
```
[1,2,3,2,1,0,1,2,3,2,1,0,1,2,3,2,1,2,3,4,3,2,1,2,3,4,3,2,1,2,3,4,3,4,5,4,3,2,1,0]
minimum = 0  maximum = 5   (limits are 0 and 5)      sum collected = 210
```
The counter reaches 5 (cook blocked) and 0 (students blocked) but never leaves [0, 5].

### `sleep()` vs `wait()` (`SleepVsWait`)
| | `Thread.sleep(ms)` | `Object.wait(ms)` |
|---|---|---|
| Monitor | **keeps** it | **releases** it |
| Belongs to | `Thread` (static) | `Object` (needs the monitor) |
| Ends | after the time | after time, `notify`/`notifyAll`, or interrupt |
| Typical use | pause | wait for a condition |

Measured: holder uses `sleep(400)` inside `synchronized` → main waited **350 ms** to enter; holder uses `wait(400)` → main entered after **0 ms**.

## Student B – Deadlock analysis and prevention

### A pair of transfers that reliably deadlocks (`DeadlockDemo`)
T1: transfer A→B (locks A, then B); T2: transfer B→A (locks B, then A); a 100 ms pause after the first lock guarantees the overlap.
```
T2 (B->A) locked Acc2(1000), waiting for Acc1(1000)
T1 (A->B) locked Acc1(1000), waiting for Acc2(1000)
after 1.5 s: T1 alive=true, T2 alive=true
JVM deadlock detector found 2 deadlocked threads:
   T1 (A->B) state=BLOCKED waits for Account@4c87… owned by T2 (B->A)
   T2 (B->A) state=BLOCKED waits for Account@6d31… owned by T1 (A->B)
```

### The four conditions present
1. **Mutual exclusion** – `synchronized` monitors allow only one owner.
2. **Hold and wait** – each thread holds one account lock while waiting for the other.
3. **No pre-emption** – a monitor cannot be taken from its owner.
4. **Circular wait** – T1 waits for T2's lock and T2 for T1's.

### Prevention by consistent lock ordering (`OrderedTransfer`)
Rule: **always lock the account with the smaller id first**, whatever the direction of the transfer.
```
the SAME two transfers with lock ordering finished: Acc1(950) Acc2(1050)
stress: 160,000 transfers finished (no deadlock), total money = 10000 (expected 10000), 233 ns/transfer
```
**Why it works:** the rule gives every lock a number and every thread acquires locks in increasing order, so any "waits-for" edge goes from a thread holding a lower-numbered lock to a higher-numbered lock. A cycle would need at least one edge going downwards, which the rule forbids – **circular wait (condition 4) is impossible**, so deadlock is impossible. The money total (10,000) confirms no transfer was lost.

## Joint conclusion – what deadlock and producer–consumer have in common
Both are about threads that must wait for something another thread controls. In both, progress needs a *protocol*, not luck. **Deadlock design:** lock ordering removes circular wait, so some thread can always proceed. **Producer–consumer design:** each thread waits on an explicit condition (not full / not empty) inside a loop, and every state change calls `notifyAll()`, so no wake-up is lost and each waiter re-checks before proceeding; the counter invariant 0 ≤ count ≤ 5 is protected by the monitor. Both guarantee progress by removing the possibility of a wait that nobody can end.

## Alternative concept
### `ArrayBlockingQueue<Integer>(5)` instead of `wait()/notifyAll()` (`QueueCompare`, 200,000 tokens, 1 cook + 2 students, sums verified)
```
wait()/notifyAll()   : 963 ns per token (put+take)
ArrayBlockingQueue(5): 1010 ns per token (put+take)
```
`queue.put(t)` / `queue.take()` replace the entire hand-written monitor logic (no loops, no `notify` choice – internally two `Condition`s on one `ReentrantLock`). Time per operation is O(1) in both, ≈ equal in this test (dominated by thread hand-off, 1 core). Space: `int[5]` + 3 ints for ours; the queue holds `Object[5]` of boxed `Integer` + lock + two conditions – a few hundred bytes, constant in both: **O(capacity)**.

### `tryLock(timeout)` instead of lock ordering (`TryLockTransfer`)
Take the first lock, `tryLock(10 ms)` the second; if it fails release the first, back off 1–5 ms and retry. This breaks **hold-and-wait**.
```
the deadlocking pair with tryLock finished: Acc1(950) Acc2(1050)  retries=1
stress: 160,000 transfers finished, total money = 10000, 1321 ns/transfer, retries=45
```
| | Lock ordering (`synchronized`) | `tryLock` with timeout |
|---|---|---|
| Time per transfer | **233 ns**, O(1) | 1,321 ns; O(1) per attempt, but retries add backoff sleeps and unbounded worst case (livelock possible) |
| Space | none extra (intrinsic monitors) | one `ReentrantLock` per account (≈ 48 B each) |
| Needs | a total order known in advance | nothing about order; also allows time-outs and error reporting |

**Less error-prone:** `ArrayBlockingQueue` is clearly less error-prone than hand-written `wait/notify` (no `if`/`while`, `notify`/`notifyAll` or lost-wake-up mistakes – our experiments produced all three bugs). Between the two deadlock strategies, lock ordering is simpler and faster whenever a global order exists (account ids); `tryLock` is the tool for cases where locks are discovered dynamically or a time-out is required, at the cost of retry logic and livelock risk.
