import java.lang.management.ManagementFactory;
import java.util.Arrays;

/** Small measurement helper: repeated timing, mean/stdev/median, allocated bytes. */
final class Bench {
    static volatile long sink;   // consumes results so the JIT cannot discard the work

    private static final com.sun.management.ThreadMXBean TM =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    /** Bytes allocated by the current thread while r runs. */
    static long allocated(Runnable r) {
        long id = Thread.currentThread().threadId();
        long before = TM.getThreadAllocatedBytes(id);
        r.run();
        return TM.getThreadAllocatedBytes(id) - before;
    }

    /** Runs r warm times (discarded) then reps times; returns ns for each measured run. */
    static double[] measure(Runnable r, int warm, int reps) {
        for (int i = 0; i < warm; i++) r.run();
        double[] ns = new double[reps];
        for (int i = 0; i < reps; i++) {
            long t0 = System.nanoTime();
            r.run();
            ns[i] = System.nanoTime() - t0;
        }
        return ns;
    }

    static double mean(double[] a) { double s = 0; for (double v : a) s += v; return s / a.length; }

    static double stdev(double[] a) {
        double m = mean(a), s = 0;
        for (double v : a) s += (v - m) * (v - m);
        return a.length > 1 ? Math.sqrt(s / (a.length - 1)) : 0;
    }

    static double median(double[] a) {
        double[] c = a.clone(); Arrays.sort(c);
        return c.length % 2 == 1 ? c[c.length / 2] : (c[c.length / 2 - 1] + c[c.length / 2]) / 2;
    }

    static long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    static void gc() {
        for (int i = 0; i < 3; i++) { System.gc(); try { Thread.sleep(30); } catch (InterruptedException e) { } }
    }
}
