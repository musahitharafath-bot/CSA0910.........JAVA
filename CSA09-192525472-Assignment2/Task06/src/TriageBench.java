import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q6: growth of admit/serve cost for PriorityQueue, sorted LinkedList and five FIFO queues, plus a 5,000-patient day. */
public class TriageBench {
    static final class Pt {
        final int severity; final long seq;
        Pt(int s, long q) { severity = s; seq = q; }
    }
    static final Comparator<Pt> CMP = Comparator.comparingInt((Pt p) -> p.severity).thenComparingLong(p -> p.seq);

    interface Triage { void admit(Pt p); Pt next(); int size(); }

    static Triage priorityQueue() {
        PriorityQueue<Pt> q = new PriorityQueue<>(CMP);
        return new Triage() { public void admit(Pt p) { q.add(p); } public Pt next() { return q.poll(); } public int size() { return q.size(); } };
    }

    static Triage sortedLinkedList() {
        LinkedList<Pt> l = new LinkedList<>();
        return new Triage() {
            public void admit(Pt p) {                         // insert before the first patient who must wait longer
                ListIterator<Pt> it = l.listIterator();
                while (it.hasNext()) { if (CMP.compare(it.next(), p) > 0) { it.previous(); break; } }
                it.add(p);
            }
            public Pt next() { return l.pollFirst(); }
            public int size() { return l.size(); }
        };
    }

    static Triage fiveQueues() {
        @SuppressWarnings("unchecked") ArrayDeque<Pt>[] qs = new ArrayDeque[5];
        for (int i = 0; i < 5; i++) qs[i] = new ArrayDeque<>();
        int[] n = {0};
        return new Triage() {
            public void admit(Pt p) { qs[p.severity - 1].addLast(p); n[0]++; }
            public Pt next() { for (ArrayDeque<Pt> q : qs) if (!q.isEmpty()) { n[0]--; return q.pollFirst(); } return null; }
            public int size() { return n[0]; }
        };
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        try (PrintWriter raw = new PrintWriter(out + "/q6_raw_runs.csv"); PrintWriter sum = new PrintWriter(out + "/q6_summary.csv")) {
            raw.println("design,n,operation,rep,ns_per_op"); sum.println("design,n,admit_ns_per_op,admit_sd,serve_ns_per_op,serve_sd,retained_bytes_per_patient");
            Map<String, java.util.function.Supplier<Triage>> designs = new LinkedHashMap<>();
            designs.put("PriorityQueue", TriageBench::priorityQueue);
            designs.put("SortedLinkedList", TriageBench::sortedLinkedList);
            designs.put("FiveFifoQueues", TriageBench::fiveQueues);
            Random rnd = new Random(21);
            for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
                Pt[] pts = new Pt[n];
                for (int i = 0; i < n; i++) pts[i] = new Pt(1 + rnd.nextInt(5), i);
                for (var e : designs.entrySet()) {
                    if (e.getKey().equals("SortedLinkedList") && n > 30_000) continue;        // O(n^2) in total: skipped, extrapolated in the report
                    int reps = n >= 1_000_000 ? 5 : 9;
                    double[] adm = new double[reps], srv = new double[reps];
                    for (int w = -2; w < reps; w++) {
                        Triage t = e.getValue().get();
                        long t0 = System.nanoTime(); for (Pt p : pts) t.admit(p); long t1 = System.nanoTime();
                        long s = 0; Pt x; while ((x = t.next()) != null) s += x.severity; long t2 = System.nanoTime();
                        Bench.sink = s;
                        if (w >= 0) { adm[w] = (t1 - t0) / (double) n; srv[w] = (t2 - t1) / (double) n; }
                    }
                    for (int i = 0; i < reps; i++) { raw.printf("%s,%d,admit,%d,%.2f%n", e.getKey(), n, i + 1, adm[i]); raw.printf("%s,%d,serve,%d,%.2f%n", e.getKey(), n, i + 1, srv[i]); }
                    double[] deltas = new double[3];
                    for (int t = 0; t < 3; t++) { Triage k = null; Bench.gc(); long h0 = Bench.usedHeap(); k = e.getValue().get(); for (Pt p : pts) k.admit(p); Bench.gc(); deltas[t] = Bench.usedHeap() - h0; Bench.sink += k.size(); }
                    double bpp = Bench.median(deltas) / n;
                    sum.printf("%s,%d,%.1f,%.1f,%.1f,%.1f,%.1f%n", e.getKey(), n, Bench.mean(adm), Bench.stdev(adm), Bench.mean(srv), Bench.stdev(srv), bpp);
                    System.out.printf("%-17s n=%-8d admit %10.1f ns/op (sd %.1f)   serve %10.1f ns/op (sd %.1f)   %.1f B/patient%n", e.getKey(), n, Bench.mean(adm), Bench.stdev(adm), Bench.mean(srv), Bench.stdev(srv), bpp);
                }
                System.out.println();
            }
        }

        // removing an arbitrary patient (left without being seen): PriorityQueue.remove(Object) is O(n)
        System.out.println("PriorityQueue.remove(Object), a patient who leaves without being seen:");
        try (PrintWriter rm = new PrintWriter(out + "/q6_remove.csv")) {
            rm.println("n,ns_per_remove");
            for (int n : new int[]{1_000, 10_000, 100_000}) {
                Pt[] pts = new Pt[n]; Random r = new Random(5);
                for (int i = 0; i < n; i++) pts[i] = new Pt(1 + r.nextInt(5), i);
                int[] pick = new int[300]; for (int i = 0; i < pick.length; i++) pick[i] = r.nextInt(n);
                double[] ns = new double[9];
                for (int w = -10; w < ns.length; w++) {
                    PriorityQueue<Pt> q = new PriorityQueue<>(CMP); for (Pt p : pts) q.add(p);
                    long t0 = System.nanoTime(); for (int i : pick) q.remove(pts[i]); long t1 = System.nanoTime();
                    if (w >= 0) ns[w] = (t1 - t0) / (double) pick.length;
                }
                rm.printf("%d,%.1f%n", n, Bench.mean(ns));
                System.out.printf("  n=%-7d %10.1f ns per remove%n", n, Bench.mean(ns));
            }
        }

        // a city hospital day: 5,000 patients, random severity, doctors serve in bursts
        System.out.println("\nSimulated day with 5,000 patients (interleaved admit/serve, peak queue length recorded):");
        try (PrintWriter dy = new PrintWriter(out + "/q6_day.csv")) {
            dy.println("rep,total_ms,peak_queue"); Random r = new Random(77); double[] tot = new double[15]; int peak = 0;
            for (int w = -3; w < tot.length; w++) {
                Triage t = priorityQueue(); int pk = 0; long seq = 0;
                long t0 = System.nanoTime();
                for (int i = 0; i < 5000; i++) {
                    t.admit(new Pt(1 + r.nextInt(5), seq++));
                    if (r.nextInt(100) < 70) t.next();           // doctors keep up with 70% of arrivals during the day
                    pk = Math.max(pk, t.size());
                }
                while (t.next() != null) { }
                long ns = System.nanoTime() - t0;
                if (w >= 0) { tot[w] = ns / 1e6; peak = pk; dy.printf("%d,%.3f,%d%n", w + 1, tot[w], pk); }
            }
            System.out.printf("  mean total time for the day: %.3f ms (sd %.3f), peak queue length %d%n", Bench.mean(tot), Bench.stdev(tot), peak);
        }
    }
}
