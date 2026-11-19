import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;

/** Q2: ArrayList vs LinkedList vs ArrayDeque vs hand-written array, per operation, three sizes. */
public class PlaylistBench {
    static final int[] SIZES = {1_000, 10_000, 100_000};
    static final int K = 1_000;          // operations per timed run for insert/remove
    static final int GETS = 10_000;      // random get(k) calls per run
    static final int WARM = 2, REPS = 7;
    static Integer[] boxed;

    /** Minimal array-backed list with manual element shifting (the 'plain array' alternative). */
    static final class ManualArray {
        Integer[] a = new Integer[10]; int n;
        void add(Integer v) { if (n == a.length) a = Arrays.copyOf(a, n * 2); a[n++] = v; }
        void insert(int idx, Integer v) {
            if (n == a.length) a = Arrays.copyOf(a, n * 2);
            for (int i = n; i > idx; i--) a[i] = a[i - 1];
            a[idx] = v; n++;
        }
        void removeAt(int idx) { for (int i = idx; i < n - 1; i++) a[i] = a[i + 1]; a[--n] = null; }
        Integer get(int i) { return a[i]; }
    }

    static List<Integer> fill(Supplier<List<Integer>> mk, int n) {
        List<Integer> l = mk.get();
        for (int i = 0; i < n; i++) l.add(boxed[i]);
        return l;
    }

    /** Time op on a freshly built structure each repetition (setup excluded). Returns ns per operation. */
    static <T> double[] timeOp(Supplier<T> setup, Consumer<T> op, int opsPerRun) {
        for (int i = 0; i < WARM; i++) op.accept(setup.get());
        double[] r = new double[REPS];
        for (int i = 0; i < REPS; i++) {
            T s = setup.get();
            long t0 = System.nanoTime();
            op.accept(s);
            r[i] = (System.nanoTime() - t0) / (double) opsPerRun;
        }
        return r;
    }

    static PrintWriter raw, sum;

    static void record(String ds, String op, int n, double[] ns) {
        for (int i = 0; i < ns.length; i++) raw.printf("%s,%s,%d,%d,%.2f%n", ds, op, n, i + 1, ns[i]);
        sum.printf("%s,%s,%d,%.2f,%.2f,%.2f%n", ds, op, n, Bench.mean(ns), Bench.stdev(ns), Bench.median(ns));
        System.out.printf("%-12s %-14s n=%-7d %12.1f ns/op (sd %.1f)%n", ds, op, n, Bench.mean(ns), Bench.stdev(ns));
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        boxed = new Integer[SIZES[SIZES.length - 1] + 2 * K + 10];
        for (int i = 0; i < boxed.length; i++) boxed[i] = i;
        Random rnd = new Random(42);
        raw = new PrintWriter(out + "/q2_raw_runs.csv"); sum = new PrintWriter(out + "/q2_summary.csv");
        raw.println("structure,operation,size,rep,ns_per_op"); sum.println("structure,operation,size,mean_ns_per_op,sd,median");

        Map<String, Supplier<List<Integer>>> lists = new LinkedHashMap<>();
        lists.put("ArrayList", ArrayList::new);
        lists.put("LinkedList", LinkedList::new);

        for (int n : SIZES) {
            int[] idx = new int[GETS];
            for (int i = 0; i < GETS; i++) idx[i] = rnd.nextInt(n);
            for (var e : lists.entrySet()) {
                String ds = e.getKey(); Supplier<List<Integer>> mk = e.getValue();
                record(ds, "add_end", n, timeOp(() -> mk.get(), l -> { for (int i = 0; i < n; i++) l.add(boxed[i]); }, n));
                record(ds, "insert_front", n, timeOp(() -> fill(mk, n), l -> { for (int i = 0; i < K; i++) l.add(0, boxed[i]); }, K));
                record(ds, "insert_middle", n, timeOp(() -> fill(mk, n), l -> { for (int i = 0; i < K; i++) l.add(l.size() / 2, boxed[i]); }, K));
                record(ds, "remove_front", n, timeOp(() -> fill(mk, n), l -> { for (int i = 0; i < K; i++) l.remove(0); }, K));
                record(ds, "remove_middle", n, timeOp(() -> fill(mk, n), l -> { for (int i = 0; i < K; i++) l.remove(l.size() / 2); }, K));
                record(ds, "get_kth", n, timeOp(() -> fill(mk, n), l -> { long s = 0; for (int i = 0; i < GETS; i++) s += l.get(idx[i]); Bench.sink = s; }, GETS));
                record(ds, "iterate", n, timeOp(() -> fill(mk, n), l -> { long s = 0; for (int v : l) s += v; Bench.sink = s; }, n));
            }
            // ArrayDeque: no index-based insert or get(k); only the end operations and iteration
            Supplier<ArrayDeque<Integer>> dq = ArrayDeque::new;
            Supplier<ArrayDeque<Integer>> dqFull = () -> { ArrayDeque<Integer> d = new ArrayDeque<>(); for (int i = 0; i < n; i++) d.addLast(boxed[i]); return d; };
            record("ArrayDeque", "add_end", n, timeOp(dq, d -> { for (int i = 0; i < n; i++) d.addLast(boxed[i]); }, n));
            record("ArrayDeque", "insert_front", n, timeOp(dqFull, d -> { for (int i = 0; i < K; i++) d.addFirst(boxed[i]); }, K));
            record("ArrayDeque", "remove_front", n, timeOp(dqFull, d -> { for (int i = 0; i < K; i++) d.pollFirst(); }, K));
            record("ArrayDeque", "iterate", n, timeOp(dqFull, d -> { long s = 0; for (int v : d) s += v; Bench.sink = s; }, n));
            // plain array with manual shifting
            Supplier<ManualArray> ma = () -> { ManualArray m = new ManualArray(); for (int i = 0; i < n; i++) m.add(boxed[i]); return m; };
            record("PlainArray", "add_end", n, timeOp(ManualArray::new, m -> { for (int i = 0; i < n; i++) m.add(boxed[i]); }, n));
            record("PlainArray", "insert_front", n, timeOp(ma, m -> { for (int i = 0; i < K; i++) m.insert(0, boxed[i]); }, K));
            record("PlainArray", "insert_middle", n, timeOp(ma, m -> { for (int i = 0; i < K; i++) m.insert(m.n / 2, boxed[i]); }, K));
            record("PlainArray", "remove_middle", n, timeOp(ma, m -> { for (int i = 0; i < K; i++) m.removeAt(m.n / 2); }, K));
            record("PlainArray", "get_kth", n, timeOp(ma, m -> { long s = 0; for (int i = 0; i < GETS; i++) s += m.get(idx[i]); Bench.sink = s; }, GETS));
        }
        raw.close(); sum.close();

        // Changed condition: 'Up Next' queue - add at both ends, remove from the front
        try (PrintWriter up = new PrintWriter(out + "/q2_upnext.csv")) {
            up.println("structure,size,rep,ns_per_cycle");
            int cycles = 10_000;
            System.out.println("\n== Up Next queue: per cycle = addLast + addFirst + 2 x removeFirst");
            for (int n : new int[]{1_000, 10_000, 100_000}) {
                Map<String, Supplier<Deque<Integer>>> dqs = new LinkedHashMap<>();
                dqs.put("LinkedList", () -> { LinkedList<Integer> l = new LinkedList<>(); for (int i = 0; i < n; i++) l.add(boxed[i]); return l; });
                dqs.put("ArrayDeque", () -> { ArrayDeque<Integer> l = new ArrayDeque<>(); for (int i = 0; i < n; i++) l.add(boxed[i]); return l; });
                for (var e : dqs.entrySet()) {
                    double[] r = timeOp(e.getValue(), d -> { for (int i = 0; i < cycles; i++) { d.addLast(boxed[i]); d.addFirst(boxed[i]); d.pollFirst(); d.pollFirst(); } }, cycles);
                    for (int i = 0; i < r.length; i++) up.printf("%s,%d,%d,%.2f%n", e.getKey(), n, i + 1, r[i]);
                    System.out.printf("%-12s n=%-7d %10.1f ns/cycle (sd %.1f)%n", e.getKey(), n, Bench.mean(r), Bench.stdev(r));
                }
                // ArrayList as the queue: add(0) / remove(0) shift the whole array
                int c2 = n >= 100_000 ? 1_000 : cycles;
                double[] r = timeOp(() -> fill(ArrayList::new, n), l -> { for (int i = 0; i < c2; i++) { l.add(boxed[i]); l.add(0, boxed[i]); l.remove(0); l.remove(0); } }, c2);
                for (int i = 0; i < r.length; i++) up.printf("ArrayList,%d,%d,%.2f%n", n, i + 1, r[i]);
                System.out.printf("%-12s n=%-7d %10.1f ns/cycle (sd %.1f)%n", "ArrayList", n, Bench.mean(r), Bench.stdev(r));
            }
        }

        // Space per element: retained heap after GC (the Integer objects are shared and excluded)
        try (PrintWriter mem = new PrintWriter(out + "/q2_memory.csv")) {
            mem.println("structure,elements,bytes_per_element");
            int n = 100_000;
            System.out.println("\n== Retained bytes per element (n = " + n + ", Integer objects excluded)");
            Map<String, Supplier<Collection<Integer>>> ms = new LinkedHashMap<>();
            ms.put("ArrayList", ArrayList::new); ms.put("LinkedList", LinkedList::new); ms.put("ArrayDeque", ArrayDeque::new);
            for (var e : ms.entrySet()) {
                double best = 0; List<Double> samples = new ArrayList<>();
                for (int t = 0; t < 5; t++) {
                    Bench.gc(); long h0 = Bench.usedHeap();
                    Collection<Integer> c = e.getValue().get(); for (int i = 0; i < n; i++) c.add(boxed[i]);
                    Bench.gc(); long h1 = Bench.usedHeap();
                    samples.add((h1 - h0) / (double) n); Bench.sink += c.size();
                }
                Collections.sort(samples); best = samples.get(samples.size() / 2);
                mem.printf("%s,%d,%.1f%n", e.getKey(), n, best);
                System.out.printf("%-12s %6.1f bytes/element%n", e.getKey(), best);
            }
        }
    }
}
