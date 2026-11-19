import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class ResultProcessor {
    static Summary process(Path dir, List<String> names, int threads, long latencyMs) {
        ConcurrentHashMap<String, FileOutcome> results = new ConcurrentHashMap<>(names.size() * 2);
        ConcurrentHashMap<Status, LongAdder> counts = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long t0 = System.nanoTime();
        try {
            List<Future<FileOutcome>> fs = new ArrayList<>(names.size());
            for (String n : names) fs.add(pool.submit(new ScoreTask(n, dir.resolve(n), latencyMs, results, counts)));
            for (int i = 0; i < fs.size(); i++) {
                String n = names.get(i);
                try { fs.get(i).get(); }
                catch (ExecutionException e) {                         // a bug inside ONE task: record it, keep going
                    ScoreTask.record(results, counts, FileOutcome.fail(n, Status.UNEXPECTED, String.valueOf(e.getCause())));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ScoreTask.record(results, counts, FileOutcome.fail(n, Status.UNEXPECTED, "interrupted"));
                    break;
                }
            }
        } finally {
            pool.shutdown();
            try { if (!pool.awaitTermination(1, TimeUnit.MINUTES)) pool.shutdownNow(); }
            catch (InterruptedException e) { pool.shutdownNow(); Thread.currentThread().interrupt(); }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        return new Summary(names.size(), ms, threads, results, counts);
    }
    static List<String> manifest(Path dir) throws java.io.IOException { return Files.readAllLines(dir.resolve("manifest.txt")); }
}
