import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q6: student-designed triage queue (no supplied code): correctness check, display, starvation and a fair rule. */
public class TriageDesign {
    static final class Pt {
        final String name; final int severity; final long arrival; final long seq;
        Pt(String n, int s, long a, long q) { name = n; severity = s; arrival = a; seq = q; }
        @Override public String toString() { return name + "(S" + severity + "@" + arrival + ")"; }
    }

    /** Strict priority: severity, then arrival time, then admission sequence (a total order, no ties). */
    static final Comparator<Pt> STRICT = Comparator.comparingInt((Pt p) -> p.severity).thenComparingLong(p -> p.arrival).thenComparingLong(p -> p.seq);

    /** Fair rule: a patient of severity s is treated as if he had arrived (s-1)*W minutes LATER. The key never changes, so the heap stays valid. */
    static Comparator<Pt> aged(long w) {
        return Comparator.comparingLong((Pt p) -> p.arrival + (p.severity - 1) * w).thenComparingInt(p -> p.severity).thenComparingLong(p -> p.seq);
    }

    static String order(PriorityQueue<Pt> q) {
        PriorityQueue<Pt> copy = new PriorityQueue<>(q);        // copy so that the live queue is untouched
        StringBuilder sb = new StringBuilder(); Pt p;
        while ((p = copy.poll()) != null) sb.append(p.name).append(' ');
        return sb.toString().trim();
    }

    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("input/patients.csv"));
        PriorityQueue<Pt> pq = new PriorityQueue<>(STRICT);
        long seq = 0;
        for (String l : lines.subList(1, lines.size())) {
            String[] p = l.split(",");
            pq.add(new Pt(p[0], Integer.parseInt(p[1]), Long.parseLong(p[2]), seq++));
        }

        System.out.println("=== Correctness check on 8 patients (hand-worked answer)");
        System.out.println("Hand-worked order : Meena Kumar Arun Lata Divya Ravi John Asha");
        System.out.println("  (S1: Meena@1, Kumar@4, Arun@7 | S2: Lata@5, Divya@6 | S3: Ravi@0, John@2 | S5: Asha@3)");
        System.out.println("Printing the PriorityQueue directly (heap array order, NOT treatment order):");
        System.out.println("  " + pq);
        StringBuilder real = new StringBuilder();
        System.out.println("Waiting list (sorted copy, O(n log n) to display): " + order(pq));
        PriorityQueue<Pt> live = new PriorityQueue<>(pq);
        Pt p; while ((p = live.poll()) != null) real.append(p.name).append(' ');
        System.out.println("Order produced by poll()                         : " + real.toString().trim());
        System.out.println("Matches hand-worked answer? " + real.toString().trim().equals("Meena Kumar Arun Lata Divya Ravi John Asha"));

        System.out.println("\n=== Starvation: 1.2 patients per minute arrive (60% S1, 40% S2); the doctor treats one per minute (overload)");
        System.out.println("A severity-5 patient (Asha) arrives at minute 0. Arrival stops at minute 3000; remaining patients are then treated.");
        System.out.printf("%-26s %-18s %-22s %-22s%n", "rule", "Asha waits (min)", "mean wait S1 (min)", "mean wait S2 (min)");
        List<String> csv = new ArrayList<>(); csv.add("rule,W_minutes,asha_wait_min,mean_wait_s1_min,mean_wait_s2_min,max_wait_any_min");
        long[] ws = {0, 10, 30, 60, 120, 240};
        for (long w : ws) {
            Comparator<Pt> cmp = w == 0 ? STRICT : aged(w);
            String label = w == 0 ? "strict severity" : "aged, W = " + w + " min/level";
            Random rnd = new Random(99);
            PriorityQueue<Pt> q = new PriorityQueue<>(cmp);
            long s = 0, ashaWait = -1, n1 = 0, n2 = 0, sum1 = 0, sum2 = 0, maxWait = 0, clock = 0;
            q.add(new Pt("Asha", 5, 0, s++));
            final int T = 3000;
            for (long t = 0; ; t++) {
                if (t < T) {
                    q.add(new Pt("P" + t, rnd.nextInt(100) < 60 ? 1 : 2, t, s++));
                    if (t % 5 == 0) q.add(new Pt("Q" + t, rnd.nextInt(100) < 60 ? 1 : 2, t, s++));   // 1.2 arrivals per minute
                }
                Pt x = q.poll();
                if (x == null) break;
                long wait = t - x.arrival; maxWait = Math.max(maxWait, wait);
                if (x.name.equals("Asha")) ashaWait = wait;
                if (x.severity == 1) { n1++; sum1 += wait; } else if (x.severity == 2) { n2++; sum2 += wait; }
            }
            System.out.printf("%-26s %-18s %-22.1f %-22.1f%n", label, ashaWait >= T ? ashaWait + " (after the surge)" : ashaWait, sum1 / (double) n1, sum2 / (double) n2);
            csv.add(String.format("%s,%d,%d,%.1f,%.1f,%d", w == 0 ? "strict" : "aged", w, ashaWait, sum1 / (double) n1, sum2 / (double) n2, maxWait));
        }
        Files.createDirectories(Path.of("output"));
        Files.write(Path.of("output/q6_aging.csv"), csv);
    }
}
