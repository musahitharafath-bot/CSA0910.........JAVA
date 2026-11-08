import java.util.*;

// Deadlock prevention by a global lock-ordering rule: always lock the account with the SMALLER id first.
public class OrderedTransfer {
    static void transfer(Account from, Account to, int amt) {
        Account first = from.id < to.id ? from : to, second = first == from ? to : from;
        synchronized (first) {
            synchronized (second) { from.balance -= amt; to.balance += amt; }
        }
    }
    static void slowTransfer(Account from, Account to, int amt) {            // same window as the deadlock demo
        Account first = from.id < to.id ? from : to, second = first == from ? to : from;
        synchronized (first) {
            try { Thread.sleep(100); } catch (InterruptedException e) { }
            synchronized (second) { from.balance -= amt; to.balance += amt; }
        }
    }
    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 1000), b = new Account(2, 1000);
        Thread t1 = new Thread(() -> slowTransfer(a, b, 100), "T1 (A->B)");
        Thread t2 = new Thread(() -> slowTransfer(b, a, 50),  "T2 (B->A)");
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("the SAME two transfers with lock ordering finished: " + a + " " + b);

        // stress: 8 threads x 20,000 random transfers among 10 accounts
        Account[] acc = new Account[10]; for (int i = 0; i < 10; i++) acc[i] = new Account(i, 1000);
        Thread[] t = new Thread[8];
        long t0 = System.nanoTime();
        for (int k = 0; k < t.length; k++) {
            final int seed = k;
            t[k] = new Thread(() -> { Random r = new Random(seed);
                for (int i = 0; i < 20_000; i++) { int x = r.nextInt(10), y = r.nextInt(10); if (x != y) transfer(acc[x], acc[y], 1 + r.nextInt(5)); } });
            t[k].start();
        }
        for (Thread x : t) x.join();
        long ns = System.nanoTime() - t0; int sum = 0; for (Account x : acc) sum += x.balance;
        System.out.printf("stress: 160,000 transfers finished (no deadlock), total money = %d (expected 10000), %.0f ns/transfer%n", sum, ns / 160000.0);
    }
}
