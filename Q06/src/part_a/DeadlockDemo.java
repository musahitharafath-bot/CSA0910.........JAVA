import java.lang.management.*;

// Student B: a pair of transfers that reliably deadlocks (A->B and B->A, each locks "from" first).
public class DeadlockDemo {
    static void transfer(Account from, Account to, int amt) {
        synchronized (from) {
            System.out.println(Thread.currentThread().getName() + " locked " + from + ", waiting for " + to);
            try { Thread.sleep(100); } catch (InterruptedException e) { }   // widen the window: both get their first lock
            synchronized (to) { from.balance -= amt; to.balance += amt; }
        }
    }
    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 1000), b = new Account(2, 1000);
        Thread t1 = new Thread(() -> transfer(a, b, 100), "T1 (A->B)");
        Thread t2 = new Thread(() -> transfer(b, a, 50),  "T2 (B->A)");
        t1.setDaemon(true); t2.setDaemon(true);            // so the JVM can still exit
        t1.start(); t2.start();
        t1.join(1500); t2.join(1500);
        System.out.println("after 1.5 s: T1 alive=" + t1.isAlive() + ", T2 alive=" + t2.isAlive());
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        if (ids == null) { System.out.println("no deadlock detected"); return; }
        System.out.println("JVM deadlock detector found " + ids.length + " deadlocked threads:");
        for (ThreadInfo ti : mx.getThreadInfo(ids))
            System.out.println("   " + ti.getThreadName() + " state=" + ti.getThreadState()
                    + " waits for " + ti.getLockName() + " owned by " + ti.getLockOwnerName());
        System.out.println("balances unchanged: " + a + " " + b);
    }
}
