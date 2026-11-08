import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class Summary {
    final int submitted; final long millis; final int threads;
    final ConcurrentHashMap<String, FileOutcome> results; final ConcurrentHashMap<Status, LongAdder> counts;
    Summary(int submitted, long millis, int threads, ConcurrentHashMap<String, FileOutcome> r, ConcurrentHashMap<Status, LongAdder> c) {
        this.submitted = submitted; this.millis = millis; this.threads = threads; results = r; counts = c;
    }
    long count(Status s) { LongAdder a = counts.get(s); return a == null ? 0 : a.sum(); }
    double averageTotal() { double t = 0; for (FileOutcome o : results.values()) if (o.status() == Status.OK) t += o.average(); return t; }
    boolean complete() { long total = 0; for (LongAdder a : counts.values()) total += a.sum(); return results.size() == submitted && total == submitted; }

    String report(int detailLines) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("files submitted : %d%n", submitted));
        for (Status s : Status.values()) sb.append(String.format("  %-10s: %d%n", s, count(s)));
        sb.append(String.format("sum of averages : %.2f (OK files)%n", averageTotal()));
        sb.append(String.format("summary complete: %s (one outcome per file, counts add up to %d)%n", complete(), submitted));
        List<FileOutcome> bad = new ArrayList<>();
        for (FileOutcome o : results.values()) if (o.status() != Status.OK) bad.add(o);
        bad.sort(Comparator.comparing(FileOutcome::file));
        sb.append("first failures  :\n");
        for (int i = 0; i < Math.min(detailLines, bad.size()); i++) sb.append("  ").append(bad.get(i).file()).append(" -> ").append(bad.get(i).status()).append(": ").append(bad.get(i).message()).append("\n");
        return sb.toString();
    }
}
