// Student A: sleep() keeps the monitor, wait() releases it.
public class SleepVsWait {
    static final Object lock = new Object();
    static long timeToEnter(boolean useWait) throws Exception {
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                try { if (useWait) lock.wait(400); else Thread.sleep(400); } catch (InterruptedException e) { }
            }
        });
        holder.start(); Thread.sleep(50);
        long t0 = System.nanoTime();
        synchronized (lock) { }                                   // how long until main can enter?
        long ms = (System.nanoTime() - t0) / 1_000_000;
        holder.join();
        return ms;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("holder calls Thread.sleep(400) inside synchronized : main waited " + timeToEnter(false) + " ms to enter");
        System.out.println("holder calls lock.wait(400)    inside synchronized : main waited " + timeToEnter(true) + " ms to enter");
    }
}
