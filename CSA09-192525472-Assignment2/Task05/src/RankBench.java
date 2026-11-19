import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/** Q5 alternative: one Comparator chain vs Comparable vs three stable sorts; also List.sort vs Collections.sort. */
public class RankBench {
    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        Comparator<Team> chain = Comparator.comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()).thenComparing(t -> t.name);
        Comparator<Team> hand = (a, b) -> {
            int c = Integer.compare(b.points, a.points); if (c != 0) return c;
            c = Integer.compare(b.goalDiff, a.goalDiff); return c != 0 ? c : a.name.compareTo(b.name);
        };
        try (PrintWriter raw = new PrintWriter(out + "/q5_raw_runs.csv"); PrintWriter sum = new PrintWriter(out + "/q5_summary.csv")) {
            raw.println("approach,n,rep,ms"); sum.println("approach,n,mean_ms,sd_ms,median_ms,sorts,bytes_allocated_per_element,identical_to_chain");
            Random rnd = new Random(3);
            for (int n : new int[]{10_000, 100_000, 1_000_000}) {
                Team[] base = new Team[n];
                for (int i = 0; i < n; i++) base[i] = new Team("Team" + rnd.nextInt(n), rnd.nextInt(100), rnd.nextInt(61) - 30);
                List<Team> reference = new ArrayList<>(Arrays.asList(base)); reference.sort(chain);
                Map<String, java.util.function.Consumer<List<Team>>> ap = new LinkedHashMap<>();
                ap.put("Comparator chain (List.sort)", l -> l.sort(chain));
                ap.put("Comparator chain (Collections.sort)", l -> Collections.sort(l, chain));
                ap.put("Hand-written comparator", l -> l.sort(hand));
                ap.put("Three stable sorts", l -> {
                    l.sort(Comparator.comparing((Team t) -> t.name));
                    l.sort(Comparator.comparingInt((Team t) -> t.goalDiff).reversed());
                    l.sort(Comparator.comparingInt((Team t) -> t.points).reversed());
                });
                for (var e : ap.entrySet()) {
                    int reps = n >= 1_000_000 ? 5 : 9;
                    for (int w = 0; w < 2; w++) e.getValue().accept(new ArrayList<>(Arrays.asList(base)));
                    double[] ms = new double[reps];
                    for (int i = 0; i < reps; i++) {
                        List<Team> l = new ArrayList<>(Arrays.asList(base));
                        long t0 = System.nanoTime(); e.getValue().accept(l); ms[i] = (System.nanoTime() - t0) / 1e6;
                        Bench.sink += l.size();
                    }
                    List<Team> l = new ArrayList<>(Arrays.asList(base));
                    long bytes = Bench.allocated(() -> e.getValue().accept(l));
                    boolean same = l.equals(reference);
                    for (int i = 0; i < ms.length; i++) raw.printf("%s,%d,%d,%.3f%n", e.getKey(), n, i + 1, ms[i]);
                    int sorts = e.getKey().startsWith("Three") ? 3 : 1;
                    sum.printf("%s,%d,%.2f,%.2f,%.2f,%d,%.1f,%b%n", e.getKey(), n, Bench.mean(ms), Bench.stdev(ms), Bench.median(ms), sorts, bytes / (double) n, same);
                    System.out.printf("%-38s n=%-8d %9.2f ms (sd %.2f)  sorts=%d  %6.1f B/element  same as chain: %b%n", e.getKey(), n, Bench.mean(ms), Bench.stdev(ms), sorts, bytes / (double) n, same);
                }
                // Comparable (separate class so the order lives inside the type)
                ComparableTeam[] ct = new ComparableTeam[n];
                for (int i = 0; i < n; i++) ct[i] = new ComparableTeam(base[i].name, base[i].points, base[i].goalDiff);
                for (int w = 0; w < 2; w++) new ArrayList<>(Arrays.asList(ct)).sort(null);
                double[] ms = new double[n >= 1_000_000 ? 5 : 9];
                for (int i = 0; i < ms.length; i++) { List<ComparableTeam> l = new ArrayList<>(Arrays.asList(ct)); long t0 = System.nanoTime(); Collections.sort(l); ms[i] = (System.nanoTime() - t0) / 1e6; Bench.sink += l.size(); }
                List<ComparableTeam> lc = new ArrayList<>(Arrays.asList(ct));
                long bytes = Bench.allocated(() -> Collections.sort(lc));
                boolean same = lc.toString().equals(reference.toString());
                for (int i = 0; i < ms.length; i++) raw.printf("Comparable (Collections.sort),%d,%d,%.3f%n", n, i + 1, ms[i]);
                sum.printf("Comparable (Collections.sort),%d,%.2f,%.2f,%.2f,1,%.1f,%b%n", n, Bench.mean(ms), Bench.stdev(ms), Bench.median(ms), bytes / (double) n, same);
                System.out.printf("%-38s n=%-8d %9.2f ms (sd %.2f)  sorts=1  %6.1f B/element  same as chain: %b%n%n", "Comparable (Collections.sort)", n, Bench.mean(ms), Bench.stdev(ms), bytes / (double) n, same);
            }
        }
    }
}
