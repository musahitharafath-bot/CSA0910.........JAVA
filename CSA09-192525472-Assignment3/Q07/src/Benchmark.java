import java.nio.file.*;
import java.util.*;

public class Benchmark {
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("data"); List<String> names = ResultProcessor.manifest(dir);
        int[] pools = {1, 2, 4, 8, 32};
        System.out.println("cores available = " + Runtime.getRuntime().availableProcessors() + ", files = " + names.size());
        for (long latency : new long[]{0, 1}) {
            int reps = latency == 0 ? 5 : 2;
            System.out.println("\n--- simulated storage latency per file: " + latency + " ms  (best of " + reps + " runs) ---");
            System.out.printf("%-8s %12s %14s %10s%n", "threads", "time (ms)", "files/second", "speed-up");
            long base = 0;
            for (int p : pools) {
                ResultProcessor.process(dir, names, p, 0);                          // warm-up
                long best = Long.MAX_VALUE;
                for (int r = 0; r < reps; r++) best = Math.min(best, ResultProcessor.process(dir, names, p, latency).millis);
                if (p == 1) base = best;
                System.out.printf("%-8d %12d %14.0f %9.2fx%n", p, best, names.size() * 1000.0 / Math.max(1, best), base / (double) Math.max(1, best));
            }
        }
    }
}
