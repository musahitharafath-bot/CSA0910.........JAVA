import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Q7 (diagnosed from Q1): the e-ticket text builder.
 * Original  : repeated String + in a loop (O(n^2) characters copied, n intermediate Strings).
 * Redesign  : a thread-confined, reusable, adaptively pre-sized StringBuilder; the finished ticket is either
 *             materialised once (R1) or copied straight into the printer buffer without creating any String (R2).
 */
public class TicketRedesign {
    static String[] template;

    static String[] makeFields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = template[i % template.length] + "#" + i;
        return f;
    }

    /** ORIGINAL: the code of the current system. */
    static String original(String[] f) {
        String t = "";
        for (int i = 0; i < f.length; i++) t = t + (i == 0 ? "" : "|") + f[i];
        return t;
    }

    /** Q1 recommendation: a new StringBuilder per ticket (no pre-sizing, so it grows by doubling). */
    static String plainBuilder(String[] f) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < f.length; i++) { if (i > 0) sb.append('|'); sb.append(f[i]); }
        return sb.toString();
    }

    /** REDESIGN: one builder per thread, reset and re-used; capacity follows the previous ticket's length. */
    static final class TicketWriter {
        private final StringBuilder sb = new StringBuilder(256);
        private int lastLength = 256;

        /** The returned CharSequence is valid only until the next call on the same thread. */
        CharSequence build(String[] f) {
            sb.setLength(0);
            sb.ensureCapacity(lastLength + 16);          // grows at most once per size jump, then never
            for (int i = 0; i < f.length; i++) { if (i > 0) sb.append('|'); sb.append(f[i]); }
            lastLength = sb.length();
            return sb;
        }

        /** R2: copy into a reusable printer page buffer - no String is created at all. */
        int buildInto(String[] f, char[] page) {
            CharSequence cs = build(f);
            int len = Math.min(cs.length(), page.length);
            sb.getChars(0, len, page, 0);
            return len;
        }
    }

    static final ThreadLocal<TicketWriter> WRITER = ThreadLocal.withInitial(TicketWriter::new);

    static String redesignString(String[] f) { return WRITER.get().build(f).toString(); }

    static long gcCount() { long c = 0; for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) c += Math.max(0, b.getCollectionCount()); return c; }

    interface Job { void run(int tickets, String[] f, char[] page); }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        template = Files.readAllLines(Path.of("input/ticket_fields.txt")).stream().filter(s -> !s.isBlank()).toArray(String[]::new);

        // ---- correctness: every variant must give the same ticket text
        String[] probe = makeFields(50);
        String expected = original(probe);
        char[] page = new char[1 << 15];
        int len = WRITER.get().buildInto(probe, page);
        boolean ok = expected.equals(plainBuilder(probe)) && expected.equals(redesignString(probe)) && expected.equals(new String(page, 0, len));
        System.out.println("Original, plain StringBuilder, redesign R1 and R2 produce identical tickets: " + ok + " (" + expected.length() + " chars)");
        if (!ok) throw new IllegalStateException("variants differ");

        Map<String, Job> variants = new LinkedHashMap<>();
        variants.put("Original (String +)", (n, f, pg) -> { long s = 0; for (int i = 0; i < n; i++) s += original(f).length(); Bench.sink = s; });
        variants.put("Plain StringBuilder (Q1)", (n, f, pg) -> { long s = 0; for (int i = 0; i < n; i++) s += plainBuilder(f).length(); Bench.sink = s; });
        variants.put("R1 reused builder -> String", (n, f, pg) -> { long s = 0; for (int i = 0; i < n; i++) s += redesignString(f).length(); Bench.sink = s; });
        variants.put("R2 reused builder -> page buffer", (n, f, pg) -> { long s = 0; TicketWriter w = WRITER.get(); for (int i = 0; i < n; i++) s += w.buildInto(f, pg); Bench.sink = s; });

        record Load(String name, int tickets, int fields) { }
        List<Load> loads = List.of(new Load("W1 short ticket (8 fields, 20000 repeats)", 20000, 8),
                new Load("W2 heavy load (10000 tickets x 50 fields)", 10000, 50),
                new Load("W3 group booking (100 tickets x 1000 fields)", 100, 1000));

        try (PrintWriter raw = new PrintWriter(out + "/q7_raw_runs.csv"); PrintWriter sum = new PrintWriter(out + "/q7_summary.csv")) {
            raw.println("workload,variant,rep,total_ns,tickets,fields");
            sum.println("workload,variant,tickets,fields,mean_ns_per_ticket,sd_ns_per_ticket,median_ns_per_ticket,bytes_per_ticket,gc_collections_per_run,output_chars");
            for (Load l : loads) {
                String[] f = makeFields(l.fields());
                int outChars = original(f).length();
                System.out.println("\n== " + l.name() + "  (ticket text = " + outChars + " chars)");
                for (var e : variants.entrySet()) {
                    Runnable r = () -> e.getValue().run(l.tickets(), f, page);
                    int warm = l.fields() >= 1000 ? 2 : 5, reps = l.fields() >= 1000 ? 8 : 15;
                    long g0 = gcCount(); double[] ns = Bench.measure(r, warm, reps); long g1 = gcCount();
                    long bytes = Bench.allocated(r);
                    for (int i = 0; i < ns.length; i++) raw.printf("\"%s\",%s,%d,%.0f,%d,%d%n", l.name(), e.getKey(), i + 1, ns[i], l.tickets(), l.fields());
                    double gcPerRun = (g1 - g0) / (double) (warm + reps);
                    sum.printf("\"%s\",%s,%d,%d,%.1f,%.1f,%.1f,%d,%.2f,%d%n", l.name(), e.getKey(), l.tickets(), l.fields(), Bench.mean(ns) / l.tickets(),
                            Bench.stdev(ns) / l.tickets(), Bench.median(ns) / l.tickets(), bytes / l.tickets(), gcPerRun, outChars);
                    System.out.printf("%-34s %12.0f ns/ticket (sd %.0f)  %10d B/ticket  %.2f GCs/run%n", e.getKey(), Bench.mean(ns) / l.tickets(), Bench.stdev(ns) / l.tickets(), bytes / l.tickets(), gcPerRun);
                }
            }
        }

        // ---- multi-threaded booking server: 2 worker threads, W2 per thread, tickets per second
        System.out.println("\n== Two worker threads, 10000 tickets x 50 fields each (throughput)");
        try (PrintWriter th = new PrintWriter(out + "/q7_threads.csv")) {
            th.println("variant,rep,threads,tickets_per_second,all_tickets_correct");
            String[] f = makeFields(50);
            final String want = expected;
            Map<String, java.util.function.Function<String[], CharSequence>> impls = new LinkedHashMap<>();
            impls.put("Original (String +)", ff -> original(ff));
            impls.put("Plain StringBuilder (Q1)", ff -> plainBuilder(ff));
            impls.put("R1/R2 ThreadLocal reused builder", ff -> WRITER.get().build(ff).toString());
            StringBuilder shared = new StringBuilder();
            impls.put("One shared builder + lock (naive reuse)", ff -> { synchronized (shared) { shared.setLength(0); for (int i = 0; i < ff.length; i++) { if (i > 0) shared.append('|'); shared.append(ff[i]); } return shared.toString(); } });
            for (var e : impls.entrySet()) {
                for (int rep = 1; rep <= 7; rep++) {
                    final int THREADS = 2, N = 10_000; final boolean[] good = {true};
                    Thread[] ts = new Thread[THREADS];
                    long t0 = System.nanoTime();
                    for (int i = 0; i < THREADS; i++) { ts[i] = new Thread(() -> { for (int k = 0; k < N; k++) if (!e.getValue().apply(f).equals(want)) good[0] = false; }); ts[i].start(); }
                    for (Thread t : ts) t.join();
                    double tps = THREADS * N / ((System.nanoTime() - t0) / 1e9);
                    if (rep > 2) th.printf("%s,%d,%d,%.0f,%b%n", e.getKey(), rep - 2, THREADS, tps, good[0]);
                    if (rep == 7) System.out.printf("%-40s %12.0f tickets/s   all tickets correct: %b%n", e.getKey(), tps, good[0]);
                }
            }
        }
    }
}
