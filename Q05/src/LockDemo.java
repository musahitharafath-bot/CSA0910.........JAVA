// Student B: which intrinsic lock does each form use?  And when static synchronization is needed.
public class LockDemo {
    static int soldAllScreens = 0;                    // shared by EVERY screen (a static field)
    static int soldAllScreensSafe = 0;

    synchronized void instanceMethod() {              // lock = this
        System.out.println("synchronized instance method: holds lock(this)=" + Thread.holdsLock(this)
                + ", holds lock(LockDemo.class)=" + Thread.holdsLock(LockDemo.class));
    }
    void block() {
        synchronized (this) { System.out.println("synchronized(this) block      : holds lock(this)=" + Thread.holdsLock(this)); }
    }
    static synchronized void staticMethod() {         // lock = LockDemo.class
        System.out.println("static synchronized method  : holds lock(LockDemo.class)=" + Thread.holdsLock(LockDemo.class));
    }

    // WRONG for a static counter: each screen object has its own 'this' lock, so two screens do not exclude each other.
    synchronized void sellInstanceLock() { int tmp = soldAllScreens; Thread.yield(); soldAllScreens = tmp + 1; }   // read-modify-write, window widened
    // RIGHT: one lock shared by all screens.
    static synchronized void sellStaticLock() { int tmp = soldAllScreensSafe; Thread.yield(); soldAllScreensSafe = tmp + 1; }

    public static void main(String[] args) throws Exception {
        LockDemo a = new LockDemo(), b = new LockDemo();
        a.instanceMethod(); a.block(); staticMethod();
        System.out.println("Do two screens share the instance lock? a==b -> " + (a == b) + " (different monitors)");
        Thread[] t = new Thread[20];
        for (int i = 0; i < 20; i++) {
            final LockDemo screen = new LockDemo();        // 20 screens
            t[i] = new Thread(() -> { for (int k = 0; k < 5_000; k++) { screen.sellInstanceLock(); sellStaticLock(); } });
        }
        for (Thread x : t) x.start(); for (Thread x : t) x.join();
        System.out.println("global ticket counter, expected 100000");
        System.out.println("  instance-synchronized : " + soldAllScreens);
        System.out.println("  static-synchronized   : " + soldAllScreensSafe);
    }
}
