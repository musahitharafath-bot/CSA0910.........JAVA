import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.IntFunction;

public class RaceExperiment {
    // 'users' threads start together; user i books seat i % seats. Returns number of successful bookings.
    static int contest(Booker b, int users, int seats) throws Exception {
        AtomicInteger ok = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        Thread[] t = new Thread[users];
        for (int i = 0; i < users; i++) {
            final int seat = i % seats;
            t[i] = new Thread(() -> { try { go.await(); } catch (InterruptedException e) { } if (b.book(seat)) ok.incrementAndGet(); });
            t[i].start();
        }
        go.countDown();
        for (Thread x : t) x.join();
        return ok.get();
    }

    static void repeat(String name, IntFunction<Booker> make, int runs) throws Exception {
        int bad = 0, max = 0; long sum = 0;
        for (int r = 0; r < runs; r++) {
            int sold = contest(make.apply(10), 100, 10);
            if (sold > 10) bad++;
            max = Math.max(max, sold); sum += sold;
        }
        System.out.printf("%-28s runs=%d  runs with double booking=%3d  max sold=%3d  average sold=%.2f%n",
                name, runs, bad, max, sum / (double) runs);
    }

    // time for 'bookings' bookings by 'threads' threads over 'seats' seats
    static double nsPerBooking(IntFunction<Booker> make, int threads, int bookings, int seats) throws Exception {
        long best = Long.MAX_VALUE;
        for (int rep = 0; rep < 7; rep++) {
            Booker b = make.apply(seats);
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            CountDownLatch go = new CountDownLatch(1);
            List<Future<?>> fs = new ArrayList<>();
            int per = bookings / threads;
            for (int k = 0; k < threads; k++) {
                final int base = k * per;
                fs.add(ex.submit(() -> { go.await(); for (int i = 0; i < per; i++) b.book((base + i) % seats); return null; }));
            }
            long t0 = System.nanoTime(); go.countDown();
            for (Future<?> f : fs) f.get();
            long t = System.nanoTime() - t0; ex.shutdown();
            if (rep >= 2) best = Math.min(best, t);          // first 2 reps are warm-up
        }
        return best / (double) bookings;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("### Student A: reproducing the race (100 threads, 10 seats, 200 runs)");
        repeat("Unsafe (no sync)", s -> new Unsafe(s, false), 200);
        repeat("Unsafe (yield in window)", s -> new Unsafe(s, true), 200);
        repeat("SyncMethod", SyncMethod::new, 200);
        repeat("SyncBlock", SyncBlock::new, 200);
        repeat("ReentrantLock", LockBooker::new, 200);
        repeat("AtomicBoolean CAS", CasBooker::new, 200);
        repeat("ConcurrentHashMap", MapBooker::new, 200);

        System.out.println();
        System.out.println("### Student B: 10,000 bookings, 8 threads, best of 5 timed repetitions (ns per booking)");
        System.out.printf("%-22s %16s %16s %22s%n", "version", "LOW contention", "HIGH contention", "with 100us I/O wait");
        System.out.printf("%-22s %16s %16s %22s%n", "", "(10,000 seats)", "(100 seats)", "(2,000 bookings)");
        Object[][] v = {
            {"SyncMethod (whole)",   (IntFunction<Booker>) SyncMethod::new},
            {"SyncBlock (critical)", (IntFunction<Booker>) SyncBlock::new},
            {"ReentrantLock",        (IntFunction<Booker>) LockBooker::new},
            {"AtomicBoolean CAS",    (IntFunction<Booker>) CasBooker::new},
            {"ConcurrentHashMap",    (IntFunction<Booker>) MapBooker::new},
        };
        for (Object[] o : v) {
            @SuppressWarnings("unchecked") IntFunction<Booker> f = (IntFunction<Booker>) o[1];
            double lo = nsPerBooking(f, 8, 10_000, 10_000), hi = nsPerBooking(f, 8, 10_000, 100);
            Work.ioNanos = 100_000; double io = nsPerBooking(f, 8, 2_000, 2_000); Work.ioNanos = 0;
            System.out.printf("%-22s %16.0f %16.0f %22.0f%n", o[0], lo, hi, io);
        }
        System.out.println("single-thread baseline (SyncBlock, no contention): "
                + String.format("%.0f", nsPerBooking(SyncBlock::new, 1, 10_000, 10_000)) + " ns per booking");
        System.out.println("(cores available: " + Runtime.getRuntime().availableProcessors() + ")");
    }
}
