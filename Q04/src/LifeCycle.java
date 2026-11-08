// Student A: states a thread passes through (NEW, RUNNABLE, TIMED_WAITING, BLOCKED, WAITING, TERMINATED).
public class LifeCycle {
    static final Object lock = new Object();
    public static void main(String[] args) throws Exception {
        Thread sleeper = new Thread(() -> { try { Thread.sleep(300); } catch (InterruptedException e) { } }, "sleeper");
        System.out.println("sleeper after new      : " + sleeper.getState());
        sleeper.start();
        System.out.println("sleeper after start    : " + sleeper.getState());
        Thread.sleep(50);
        System.out.println("sleeper inside sleep() : " + sleeper.getState());
        sleeper.join();
        System.out.println("sleeper after finish   : " + sleeper.getState());

        Thread blocked, waiter;
        synchronized (lock) {                                   // main holds the lock
            blocked = new Thread(() -> { synchronized (lock) { } }, "blocked");
            blocked.start(); Thread.sleep(50);
            System.out.println("blocked, lock held     : " + blocked.getState());
        }
        blocked.join();
        waiter = new Thread(() -> { synchronized (lock) { try { lock.wait(); } catch (InterruptedException e) { } } }, "waiter");
        waiter.start(); Thread.sleep(50);
        System.out.println("waiter inside wait()   : " + waiter.getState());
        synchronized (lock) { lock.notify(); }
        waiter.join();
        System.out.println("waiter after notify    : " + waiter.getState());
    }
}
