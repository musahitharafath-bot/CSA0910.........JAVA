import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/** Q1: String +, StringBuilder, StringBuffer (and join / StringJoiner / format) for e-ticket text. */
public class TicketBench {
    interface Builder { String build(String[] fields); }
    record Workload(String name, int tickets, int fields) { }

    static String[] template;
    static final Map<Integer, String> FORMATS = new HashMap<>();

    static String[] makeFields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = template[i % template.length] + "#" + i;
        return f;
    }

    static String fmt(int n) {
        return FORMATS.computeIfAbsent(n, k -> "%s|".repeat(k - 1) + "%s");
    }

    static Map<String, Builder> approaches() {
        Map<String, Builder> m = new LinkedHashMap<>();
        m.put("String +", f -> {
            String t = "";
            for (int i = 0; i < f.length; i++) t = t + (i == 0 ? "" : "|") + f[i];
            return t;
        });
        m.put("StringBuilder", f -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < f.length; i++) { if (i > 0) sb.append('|'); sb.append(f[i]); }
            return sb.toString();
        });
        m.put("StringBuffer", f -> {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < f.length; i++) { if (i > 0) sb.append('|'); sb.append(f[i]); }
            return sb.toString();
        });
        m.put("String.join", f -> String.join("|", f));
        m.put("StringJoiner", f -> {
            StringJoiner j = new StringJoiner("|");
            for (String s : f) j.add(s);
            return j.toString();
        });
        m.put("String.format", f -> String.format(fmt(f.length), (Object[]) f));
        return m;
    }

    static int bufferExpansions(String[] f) {
        StringBuilder sb = new StringBuilder();
        int last = sb.capacity(), grows = 0;
        for (int i = 0; i < f.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(f[i]);
            if (sb.capacity() != last) { grows++; last = sb.capacity(); }
        }
        return grows;
    }

    static String estimateIntermediate(String name, int n) {
        return switch (name) {
            case "String +" -> String.valueOf(n - 1);
            case "StringBuilder", "StringBuffer" -> "0 (+" + bufferExpansions(makeFields(n)) + " array growths)";
            case "String.join", "StringJoiner" -> "0 (1 helper array)";
            default -> "~" + n + " (format-specifier objects)";
        };
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        template = Files.readAllLines(Path.of("input/ticket_fields.txt")).stream()
                .filter(s -> !s.isBlank()).toArray(String[]::new);

        // all approaches must produce identical text
        Map<String, Builder> ap = approaches();
        String[] probe = makeFields(50);
        String expected = ap.get("StringBuilder").build(probe);
        for (var e : ap.entrySet())
            if (!e.getValue().build(probe).equals(expected)) throw new IllegalStateException(e.getKey() + " differs");
        System.out.println("All six approaches produce identical 50-field tickets (" + expected.length() + " chars).");

        List<Workload> loads = List.of(
                new Workload("W1 short ticket (8 fields, 20000 repeats)", 20000, 8),
                new Workload("W2 heavy load (10000 tickets x 50 fields)", 10000, 50),
                new Workload("W3 group booking (100 tickets x 1000 fields)", 100, 1000));

        try (PrintWriter raw = new PrintWriter(out + "/q1_raw_runs.csv");
             PrintWriter sum = new PrintWriter(out + "/q1_summary.csv")) {
            raw.println("workload,approach,rep,total_ns,tickets,fields");
            sum.println("workload,approach,tickets,fields,mean_ns_per_ticket,sd_ns_per_ticket,median_ns_per_ticket,bytes_per_ticket,heap_delta_kb_no_gc,est_intermediate_strings_per_ticket");
            for (Workload w : loads) {
                String[] f = makeFields(w.fields());
                System.out.println("\n== " + w.name());
                for (var e : ap.entrySet()) {
                    Builder b = e.getValue();
                    Runnable run = () -> { long s = 0; for (int t = 0; t < w.tickets(); t++) s += b.build(f).length(); Bench.sink = s; };
                    int warm = w.fields() >= 1000 ? 2 : 5, reps = w.fields() >= 1000 ? 8 : 15;
                    double[] ns = Bench.measure(run, warm, reps);
                    for (int i = 0; i < ns.length; i++) raw.printf("\"%s\",%s,%d,%.0f,%d,%d%n", w.name(), e.getKey(), i + 1, ns[i], w.tickets(), w.fields());
                    long bytes = Bench.allocated(run);
                    Bench.gc();
                    long h0 = Bench.usedHeap(); run.run(); long h1 = Bench.usedHeap();
                    double per = Bench.mean(ns) / w.tickets();
                    String est = estimateIntermediate(e.getKey(), w.fields());
                    sum.printf("\"%s\",%s,%d,%d,%.1f,%.1f,%.1f,%d,%d,\"%s\"%n", w.name(), e.getKey(), w.tickets(), w.fields(),
                            per, Bench.stdev(ns) / w.tickets(), Bench.median(ns) / w.tickets(), bytes / w.tickets(), (h1 - h0) / 1024, est);
                    System.out.printf("%-14s %12.0f ns/ticket (sd %.0f)  %10d B/ticket  heap+%d KB  intermediates: %s%n",
                            e.getKey(), per, Bench.stdev(ns) / w.tickets(), bytes / w.tickets(), (h1 - h0) / 1024, est);
                }
            }
        }

        // Scaling sweep: one ticket, n fields, + versus StringBuilder
        try (PrintWriter sc = new PrintWriter(out + "/q1_scaling.csv")) {
            sc.println("fields,plus_ns,builder_ns,plus_bytes,builder_bytes,ratio_plus_over_builder");
            System.out.println("\n== Scaling sweep (one ticket)");
            Builder plus = ap.get("String +"), sb = ap.get("StringBuilder");
            for (int n = 100; n <= 12800; n *= 2) {
                String[] f = makeFields(n);
                int reps = n >= 6400 ? 10 : 20, inner = Math.max(1, 200000 / n / 10);
                double tp = Bench.mean(Bench.measure(() -> { for (int i = 0; i < inner; i++) Bench.sink += plus.build(f).length(); }, 5, reps)) / inner;
                double ts = Bench.mean(Bench.measure(() -> { for (int i = 0; i < inner; i++) Bench.sink += sb.build(f).length(); }, 5, reps)) / inner;
                long bp = Bench.allocated(() -> Bench.sink += plus.build(f).length());
                long bs = Bench.allocated(() -> Bench.sink += sb.build(f).length());
                sc.printf("%d,%.0f,%.0f,%d,%d,%.2f%n", n, tp, ts, bp, bs, tp / ts);
                System.out.printf("n=%5d  +: %12.0f ns  SB: %9.0f ns  ratio %.1fx   bytes +: %d  SB: %d%n", n, tp, ts, tp / ts, bp, bs);
            }
        }
    }
}
