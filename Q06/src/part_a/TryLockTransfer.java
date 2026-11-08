import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// Alternative: no ordering rule; take the first lock, tryLock(timeout) the second, back off and retry on failure.
public class TryLockTransfer {
    static final AtomicLong retries = new AtomicLong();
    static void transfer(Account from, Account to, int amt, boolean slow) throws InterruptedException {
        while (true) {
            from.lock.lock();
            try {
                if (slow) Thread.sleep(100);
                if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                    try { from.balance -= amt; to.balance += amt; return; } finally { to.lock.unlock(); }
                }
            } finally { from.lock.unlock(); }
            retries.incrementAndGet();                              // could not get both: release, back off, retry
            Thread.sleep(1 + (long) (Math.random() * 5));
        }
    }
    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 1000), b = new Account(2, 1000);
        Thread t1 = new Thread(() -> { try { transfer(a, b, 100, true); } catch (InterruptedException e) { } });
        Thread t2 = new Thread(() -> { try { transfer(b, a, 50, true); } catch (InterruptedException e) { } });
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("the deadlocking pair with tryLock finished: " + a + " " + b + "  retries=" + retries.get());

        retries.set(0);
        Account[] acc = new Account[10]; for (int i = 0; i < 10; i++) acc[i] = new Account(i, 1000);
        Thread[] t = new Thread[8];
        long t0 = System.nanoTime();
        for (int k = 0; k < t.length; k++) {
            final int seed = k;
            t[k] = new Thread(() -> { Random r = new Random(seed);
                try { for (int i = 0; i < 20_000; i++) { int x = r.nextInt(10), y = r.nextInt(10); if (x != y) transfer(acc[x], acc[y], 1 + r.nextInt(5), false); } }
                catch (InterruptedException e) { } });
            t[k].start();
        }
        for (Thread x : t) x.join();
        long ns = System.nanoTime() - t0; int sum = 0; for (Account x : acc) sum += x.balance;
        System.out.printf("stress: 160,000 transfers finished, total money = %d (expected 10000), %.0f ns/transfer, retries=%d%n", sum, ns / 160000.0, retries.get());
    }
}
