import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q3 alternatives: HashSet<Student>, TreeSet<Student> with Comparator, HashMap<String,Student>. */
public class DedupBench {
    static class Student {
        final String regNo, name;
        Student(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof Student s && regNo.equals(s.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        try (PrintWriter raw = new PrintWriter(out + "/q3_raw_runs.csv"); PrintWriter sum = new PrintWriter(out + "/q3_summary.csv")) {
            raw.println("structure,scans,unique,rep,ns_per_scan");
            sum.println("structure,scans,unique,mean_ns_per_scan,sd,median,retained_bytes_per_unique_student");
            Random rnd = new Random(7);
            for (int scans : new int[]{10_000, 100_000, 1_000_000}) {
                int unique = scans / 2;
                String[] regs = new String[unique];
                for (int i = 0; i < unique; i++) regs[i] = String.format("1923%06d", i);
                Student[] stream = new Student[scans];
                for (int i = 0; i < scans; i++) { String r = regs[rnd.nextInt(unique)]; stream[i] = new Student(r, "n"); }
                Set<String> distinct = new HashSet<>(); for (Student s : stream) distinct.add(s.regNo);
                int uniq = distinct.size();
                Comparator<Student> byReg = Comparator.comparing(s -> s.regNo);

                Map<String, Runnable> runs = new LinkedHashMap<>();
                runs.put("HashSet<Student>", () -> { Set<Student> s = new HashSet<>(); for (Student st : stream) s.add(st); Bench.sink = s.size(); });
                runs.put("TreeSet<Student>", () -> { Set<Student> s = new TreeSet<>(byReg); for (Student st : stream) s.add(st); Bench.sink = s.size(); });
                runs.put("HashMap<String,Student>", () -> { Map<String, Student> m = new HashMap<>(); for (Student st : stream) m.putIfAbsent(st.regNo, st); Bench.sink = m.size(); });

                for (var e : runs.entrySet()) {
                    double[] ns = Bench.measure(e.getValue(), 3, 9);
                    for (int i = 0; i < ns.length; i++) raw.printf("\"%s\",%d,%d,%d,%.2f%n", e.getKey(), scans, uniq, i + 1, ns[i] / scans);
                    // retained size of the result structure (Student objects pre-exist and are excluded)
                    Bench.gc(); long h0 = Bench.usedHeap();
                    Object keep = null;
                    switch (e.getKey()) {
                        case "HashSet<Student>" -> { Set<Student> s = new HashSet<>(); for (Student st : stream) s.add(st); keep = s; }
                        case "TreeSet<Student>" -> { Set<Student> s = new TreeSet<>(byReg); for (Student st : stream) s.add(st); keep = s; }
                        default -> { Map<String, Student> m = new HashMap<>(); for (Student st : stream) m.putIfAbsent(st.regNo, st); keep = m; }
                    }
                    Bench.gc(); long h1 = Bench.usedHeap(); Bench.sink += keep.hashCode() & 1;
                    double bytes = (h1 - h0) / (double) uniq;
                    sum.printf("\"%s\",%d,%d,%.2f,%.2f,%.2f,%.1f%n", e.getKey(), scans, uniq, Bench.mean(ns) / scans, Bench.stdev(ns) / scans, Bench.median(ns) / scans, bytes);
                    System.out.printf("%-24s scans=%-8d unique=%-7d %8.1f ns/scan (sd %.1f)  %6.1f B/unique%n", e.getKey(), scans, uniq, Bench.mean(ns) / scans, Bench.stdev(ns) / scans, bytes);
                }
            }
        }
    }
}
