import java.lang.management.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

// Alternative concepts: parallelStream() and one thread per file.
public class Alternatives {
    static FileOutcome handle(String name, Path dir) throws Exception {                     // same per-file work as ScoreTask
        if (name.contains("poison")) throw new IllegalStateException("simulated bug while processing " + name);
        try { return ScoreParser.parse(name, dir.resolve(name)); }
        catch (java.nio.file.NoSuchFileException e) { return FileOutcome.fail(name, Status.MISSING, "file not found"); }
        catch (java.io.IOException e)               { return FileOutcome.fail(name, Status.IO_ERROR, e.getMessage()); }
        catch (ScoreFormatException e)              { return FileOutcome.fail(name, Status.CORRUPT, e.getMessage()); }
        catch (ScoreValidationException e)          { return FileOutcome.fail(name, Status.INVALID, e.getMessage()); }
    }
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("data"); List<String> names = ResultProcessor.manifest(dir);
        System.out.println("cores = " + Runtime.getRuntime().availableProcessors() + ", common pool parallelism = " + ForkJoinPool.commonPool().getParallelism());

        System.out.println("\n[1] parallelStream WITHOUT per-element try/catch (a bug in one file)");
        try {
            long c = names.parallelStream().map(n -> { try { return handle(n, dir); } catch (Exception e) { throw new RuntimeException(e); } }).collect(Collectors.toList()).size();
            System.out.println("    finished, " + c + " results");
        } catch (RuntimeException e) {
            System.out.println("    WHOLE STREAM FAILED: " + e.getCause() + "  -> no summary is produced at all");
        }

        System.out.println("\n[2] parallelStream WITH per-element try/catch (same outcomes as the executor design)");
        long t0 = System.nanoTime();
        Map<Status, Long> m = names.parallelStream().map(n -> { try { return handle(n, dir).status(); } catch (Exception e) { return Status.UNEXPECTED; } })
                .collect(Collectors.groupingBy(s -> s, () -> new EnumMap<>(Status.class), Collectors.counting()));
        System.out.println("    counts = " + m + "  time = " + (System.nanoTime() - t0) / 1_000_000 + " ms");

        System.out.println("\n[3] parallelStream with 1 ms simulated latency per file (blocking I/O inside a stream)");
        t0 = System.nanoTime();
        names.parallelStream().forEach(n -> { try { Thread.sleep(1); } catch (InterruptedException e) { } });
        System.out.println("    time = " + (System.nanoTime() - t0) / 1_000_000 + " ms for " + names.size() + " files (common pool has only " + ForkJoinPool.commonPool().getParallelism() + " worker(s) + caller)");

        System.out.println("\n[4] one thread per file (10,000 threads), pure processing, no latency");
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        AtomicInteger done = new AtomicInteger(), unexpected = new AtomicInteger(); AtomicInteger live = new AtomicInteger(), peak = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        t0 = System.nanoTime();
        try {
            for (String n : names) {
                Thread t = new Thread(() -> {
                    int l = live.incrementAndGet(); peak.accumulateAndGet(l, Math::max);
                    try { handle(n, dir); } catch (Exception e) { unexpected.incrementAndGet(); }
                    finally { live.decrementAndGet(); done.incrementAndGet(); }
                });
                t.setUncaughtExceptionHandler((th, ex) -> { });
                ts.add(t); t.start();
            }
            for (Thread t : ts) t.join();
            System.out.println("    done=" + done.get() + "  unexpected failures caught=" + unexpected.get() + "  time=" + (System.nanoTime() - t0) / 1_000_000 + " ms");
        } catch (Throwable e) {
            System.out.println("    FAILED after creating " + ts.size() + " threads: " + e);
        }
        System.out.println("    peak simultaneously live task threads = " + peak.get() + ", threads started in total = " + ts.size()
                + "; worst case memory = threads x stack (1 MB) = " + ts.size() + " MB reserved if all were alive at once");
    }
}
