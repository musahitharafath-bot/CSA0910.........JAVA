import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Alternative concept: ExecutorService with a fixed pool. n scripts are split into batches of 10;
// each batch costs ~20 ms. Expected time ~ ceil(batches / p) * 20 ms.
public class PoolDemo {
    static final int N = 400, BATCH = 10, BATCH_MS = 20;
    static int batches() { return N / BATCH; }

    static void work() { try { Thread.sleep(BATCH_MS); } catch (InterruptedException e) { } }

    static long withThreads() throws Exception {                 // one Thread object per batch
        long t0 = System.nanoTime();
        Thread[] t = new Thread[batches()];
        for (int i = 0; i < t.length; i++) { t[i] = new Thread(PoolDemo::work); t[i].start(); }
        for (Thread x : t) x.join();
        return (System.nanoTime() - t0) / 1_000_000;
    }
    static final AtomicInteger created = new AtomicInteger();
    static long withPool(int p) throws Exception {
        created.set(0);
        long t0 = System.nanoTime();
        ExecutorService ex = Executors.newFixedThreadPool(p, r -> { created.incrementAndGet(); return new Thread(r); });
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < batches(); i++) fs.add(ex.submit(PoolDemo::work));
        for (Future<?> f : fs) f.get();
        ex.shutdown();
        return (System.nanoTime() - t0) / 1_000_000;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("n = " + N + " scripts, " + batches() + " batches of " + BATCH + ", " + BATCH_MS + " ms per batch");
        System.out.printf("%-26s %10s %14s %16s%n", "approach", "time(ms)", "expected(ms)", "threads created");
        long t = withThreads();
        System.out.printf("%-26s %10d %14d %16d%n", "Thread per batch", t, BATCH_MS, batches());
        for (int p : new int[]{1, 2, 4, 8}) {
            t = withPool(p);
            System.out.printf("%-26s %10d %14d %16d%n", "FixedThreadPool(" + p + ")", t,
                    (long) Math.ceil(batches() / (double) p) * BATCH_MS, created.get());
        }
        System.out.println("Default thread stack size (-Xss) reserved per thread: 1024 KB -> "
                + batches() + " threads reserve ~" + batches() + " MB, a pool of 4 reserves ~4 MB");
    }
}
