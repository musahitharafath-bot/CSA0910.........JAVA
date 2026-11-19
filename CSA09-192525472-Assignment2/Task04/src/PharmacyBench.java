import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Q4: HashMap, TreeMap, LinkedHashMap (plus Hashtable, ConcurrentHashMap, sorted array) for medicine stock. */
public class PharmacyBench {
    static final class Medicine {
        final String code; final String name; int qty; final int seq;
        Medicine(String c, String n, int q, int s) { code = c; name = n; qty = q; seq = s; }
    }

    static final int LOOKUPS = 100_000;

    static Medicine[] generate(int n, Random rnd) {
        int[] ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = rnd.nextInt(i + 1); int t = ids[i]; ids[i] = ids[j]; ids[j] = t; }   // shuffled codes
        Medicine[] m = new Medicine[n];
        for (int i = 0; i < n; i++) m[i] = new Medicine(String.format("MED%07d", ids[i]), "Medicine " + i, 100 + i % 900, i);
        return m;
    }

    static String report(Collection<Medicine> c) {
        StringBuilder sb = new StringBuilder(c.size() * 20);
        for (Medicine m : c) sb.append(m.code).append(' ').append(m.qty).append('\n');
        return sb.toString();
    }

    static String reportCodeOrderFromHash(Map<String, Medicine> map) {          // wrong map for this report: sort first
        List<Medicine> l = new ArrayList<>(map.values());
        l.sort(Comparator.comparing(m -> m.code));
        return report(l);
    }

    static String reportAddedOrderFromSorted(Map<String, Medicine> map) {       // wrong map: sort by sequence number
        List<Medicine> l = new ArrayList<>(map.values());
        l.sort(Comparator.comparingInt(m -> m.seq));
        return report(l);
    }

    static double ms(double[] ns) { return Bench.mean(ns) / 1e6; }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        Random rnd = new Random(11);
        try (PrintWriter raw = new PrintWriter(out + "/q4_raw_runs.csv");
             PrintWriter sum = new PrintWriter(out + "/q4_summary.csv");
             PrintWriter lat = new PrintWriter(out + "/q4_latency.csv")) {
            raw.println("structure,medicines,metric,rep,ms");
            sum.println("structure,medicines,build_ms,lookups_100k_ms,ns_per_lookup,sd_ms_lookups,code_report_ms,added_report_ms,code_order_correct_natively,added_order_correct_natively,retained_bytes_per_medicine");
            lat.println("structure,medicines,p50_ns,p99_ns,max_ns");

            for (int n : new int[]{1_000, 10_000, 100_000, 500_000}) {
                Medicine[] meds = generate(n, rnd);
                String[] probe = new String[LOOKUPS];
                for (int i = 0; i < LOOKUPS; i++) probe[i] = meds[rnd.nextInt(n)].code;
                Medicine[] byCode = meds.clone();
                Arrays.sort(byCode, Comparator.comparing(m -> m.code));
                String wantCode = report(Arrays.asList(byCode)), wantAdded = report(Arrays.asList(meds));

                Map<String, Supplier<Map<String, Medicine>>> kinds = new LinkedHashMap<>();
                kinds.put("HashMap", HashMap::new);
                kinds.put("TreeMap", TreeMap::new);
                kinds.put("LinkedHashMap", LinkedHashMap::new);
                kinds.put("Hashtable", Hashtable::new);
                kinds.put("ConcurrentHashMap", ConcurrentHashMap::new);

                for (var e : kinds.entrySet()) {
                    String k = e.getKey(); Supplier<Map<String, Medicine>> mk = e.getValue();
                    int reps = n >= 500_000 ? 5 : 9;
                    double[] build = Bench.measure(() -> { Map<String, Medicine> m = mk.get(); for (Medicine x : meds) m.put(x.code, x); Bench.sink = m.size(); }, 2, reps);
                    final Map<String, Medicine> map = mk.get(); for (Medicine x : meds) map.put(x.code, x);
                    double[] look = Bench.measure(() -> { long s = 0; for (String c : probe) s += map.get(c).qty; Bench.sink = s; }, 3, reps + 3);
                    boolean codeNat = false, addedNat = false; double repCode, repAdded;
                    double[] rc, ra;
                    if (k.equals("TreeMap")) {
                        rc = Bench.measure(() -> Bench.sink = report(map.values()).length(), 2, reps);
                        ra = Bench.measure(() -> Bench.sink = reportAddedOrderFromSorted(map).length(), 2, reps);
                    } else if (k.equals("LinkedHashMap")) {
                        rc = Bench.measure(() -> Bench.sink = reportCodeOrderFromHash(map).length(), 2, reps);
                        ra = Bench.measure(() -> Bench.sink = report(map.values()).length(), 2, reps);
                    } else {
                        rc = Bench.measure(() -> Bench.sink = reportCodeOrderFromHash(map).length(), 2, reps);
                        ra = Bench.measure(() -> Bench.sink = reportAddedOrderFromSorted(map).length(), 2, reps);
                    }
                    String natural = report(map.values());
                    codeNat = natural.equals(wantCode); addedNat = natural.equals(wantAdded);
                    // retained size: median of 5 trials (heap deltas are noisy for small maps)
                    Map<String, Medicine> keep = null; double[] deltas = new double[5];
                    for (int t = 0; t < deltas.length; t++) {
                        keep = null; Bench.gc(); long h0 = Bench.usedHeap();
                        keep = mk.get(); for (Medicine x : meds) keep.put(x.code, x);
                        Bench.gc(); long h1 = Bench.usedHeap(); Bench.sink += keep.size();
                        deltas[t] = h1 - h0;
                    }
                    long h0 = 0, h1 = (long) Bench.median(deltas);
                    // latency distribution of single lookups
                    long[] one = new long[20_000];
                    for (int i = 0; i < one.length; i++) { String c = probe[i]; long t0 = System.nanoTime(); Bench.sink += keep.get(c).qty; one[i] = System.nanoTime() - t0; }
                    Arrays.sort(one);
                    for (int i = 0; i < build.length; i++) raw.printf("%s,%d,build,%d,%.3f%n", k, n, i + 1, build[i] / 1e6);
                    for (int i = 0; i < look.length; i++) raw.printf("%s,%d,lookups_100k,%d,%.3f%n", k, n, i + 1, look[i] / 1e6);
                    for (int i = 0; i < rc.length; i++) raw.printf("%s,%d,code_report,%d,%.3f%n", k, n, i + 1, rc[i] / 1e6);
                    for (int i = 0; i < ra.length; i++) raw.printf("%s,%d,added_report,%d,%.3f%n", k, n, i + 1, ra[i] / 1e6);
                    sum.printf("%s,%d,%.2f,%.3f,%.1f,%.3f,%.2f,%.2f,%b,%b,%.1f%n", k, n, ms(build), ms(look), Bench.mean(look) / LOOKUPS, Bench.stdev(look) / 1e6,
                            ms(rc), ms(ra), codeNat, addedNat, (h1 - h0) / (double) n);
                    lat.printf("%s,%d,%d,%d,%d%n", k, n, one[one.length / 2], one[(int) (one.length * 0.99)], one[one.length - 1]);
                    System.out.printf("%-17s n=%-7d build %8.2f ms | 100k lookups %7.2f ms (%.0f ns each) | code report %8.2f ms%s | added report %8.2f ms%s | p50 %d ns p99 %d ns | %.0f B/med%n",
                            k, n, ms(build), ms(look), Bench.mean(look) / LOOKUPS, ms(rc), codeNat ? " [native]" : " [sorted]", ms(ra), addedNat ? " [native]" : " [sorted]",
                            one[one.length / 2], one[(int) (one.length * 0.99)], (h1 - h0) / (double) n);
                }

                // sorted array + binary search
                Medicine[] sorted = byCode.clone();
                String[] keys = new String[n]; for (int i = 0; i < n; i++) keys[i] = sorted[i].code;
                double[] build = Bench.measure(() -> { Medicine[] c = meds.clone(); Arrays.sort(c, Comparator.comparing(m -> m.code)); Bench.sink = c.length; }, 2, 7);
                double[] look = Bench.measure(() -> { long s = 0; for (String c : probe) s += sorted[Arrays.binarySearch(keys, c)].qty; Bench.sink = s; }, 3, 10);
                double[] rep = Bench.measure(() -> Bench.sink = report(Arrays.asList(sorted)).length(), 2, 7);
                // one insertion into the sorted array (shift)
                int inserts = 200;
                double[] ins = Bench.measure(() -> {
                    String[] k2 = Arrays.copyOf(keys, n + inserts); Medicine[] m2 = Arrays.copyOf(sorted, n + inserts); int size = n;
                    for (int i = 0; i < inserts; i++) {
                        String nk = String.format("MED9%06d", i); int pos = -Arrays.binarySearch(k2, 0, size, nk) - 1;
                        System.arraycopy(k2, pos, k2, pos + 1, size - pos); System.arraycopy(m2, pos, m2, pos + 1, size - pos);
                        k2[pos] = nk; m2[pos] = sorted[0]; size++;
                    }
                    Bench.sink = size;
                }, 2, 7);
                for (int i = 0; i < build.length; i++) raw.printf("SortedArray+binarySearch,%d,build,%d,%.3f%n", n, i + 1, build[i] / 1e6);
                for (int i = 0; i < look.length; i++) raw.printf("SortedArray+binarySearch,%d,lookups_100k,%d,%.3f%n", n, i + 1, look[i] / 1e6);
                for (int i = 0; i < rep.length; i++) raw.printf("SortedArray+binarySearch,%d,code_report,%d,%.3f%n", n, i + 1, rep[i] / 1e6);
                for (int i = 0; i < ins.length; i++) raw.printf("SortedArray+binarySearch,%d,insert_200,%d,%.3f%n", n, i + 1, ins[i] / 1e6);
                sum.printf("SortedArray+binarySearch,%d,%.2f,%.3f,%.1f,%.3f,%.2f,,true,false,%.1f%n", n, ms(build), ms(look), Bench.mean(look) / LOOKUPS, Bench.stdev(look) / 1e6, ms(rep), 4.0);
                System.out.printf("%-17s n=%-7d build(sort) %8.2f ms | 100k lookups %7.2f ms (%.0f ns each) | code report %8.2f ms | 200 inserts %.2f ms (%.0f us each)%n",
                        "SortedArray", n, ms(build), ms(look), Bench.mean(look) / LOOKUPS, ms(rep), ms(ins), Bench.mean(ins) / inserts / 1e3);
                System.out.println();
            }
        }
    }
}
