import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q3: equals/hashCode contract experiments. Each test prints its prediction first. */
public class ContractExperiments {

    /** The junior developer's class: equals() on regNo, hashCode() NOT overridden. */
    static class BadStudent {
        final String regNo, name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof BadStudent b && regNo.equals(b.regNo); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Corrected class: hashCode() uses exactly the field used by equals(). */
    static class GoodStudent {
        final String regNo, name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof GoodStudent g && regNo.equals(g.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Mutable key: hashCode depends on a field that can change after insertion. */
    static class MutableStudent {
        String regNo; final String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof MutableStudent m && regNo.equals(m.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Legal but terrible: every object gets the same hash code. */
    static class ConstantHashStudent {
        final String regNo;
        ConstantHashStudent(String r) { regNo = r; }
        @Override public boolean equals(Object o) { return o instanceof ConstantHashStudent c && regNo.equals(c.regNo); }
        @Override public int hashCode() { return 42; }
    }

    static int bucket(int h, int tableSize) { return (h ^ (h >>> 16)) & (tableSize - 1); }

    static void header(String t) { System.out.println("\n=== " + t); }

    public static void main(String[] args) throws Exception {
        List<String[]> scans = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of("input/scans.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            scans.add(line.split(","));
        }

        // ---------- Test 1: the HashSet appears to work ----------
        header("Test 1 - the same card objects are re-used at each gate");
        BadStudent a = new BadStudent("192311001", "Arun"), b = new BadStudent("192311002", "Divya");
        System.out.println("Prediction: 2. Adding the SAME object twice gives the same identity hash, the same bucket and equals() is true (== check).");
        Set<BadStudent> s1 = new HashSet<>(List.of(a, b));
        s1.add(a);
        System.out.println("Observed size = " + s1.size() + "   (BadStudent works only because the references are identical)");

        // ---------- Test 2: duplicates exposed ----------
        header("Test 2 - a NEW Student object is created for every scan (8 scans, 5 distinct students)");
        System.out.println("Prediction: BadStudent -> 8 (every object has a different identity hash code, so equals() is never even called); GoodStudent -> 5.");
        Set<BadStudent> bad = new HashSet<>(); Set<GoodStudent> good = new HashSet<>();
        for (String[] s : scans) { bad.add(new BadStudent(s[1], s[2])); good.add(new GoodStudent(s[1], s[2])); }
        System.out.println("Observed BadStudent  size = " + bad.size());
        System.out.println("Observed GoodStudent size = " + good.size());
        BadStudent x1 = new BadStudent("192311001", "Arun"), x2 = new BadStudent("192311001", "Arun");
        System.out.println("x1.equals(x2) = " + x1.equals(x2) + ", hashCodes " + x1.hashCode() + " vs " + x2.hashCode()
                + ", buckets (table 16) " + bucket(x1.hashCode(), 16) + " vs " + bucket(x2.hashCode(), 16));
        System.out.println("GoodStudent hash for 192311001 = " + new GoodStudent("192311001", "Arun").hashCode());

        // ---------- Test 3a: changing a hashed field after insertion ----------
        header("Test 3a - regNo is changed after the object was added (hashCode uses regNo)");
        System.out.println("Prediction: size stays 1, but contains(s) becomes false and remove(s) fails because the object sits in the old bucket.");
        Set<MutableStudent> ms = new HashSet<>();
        MutableStudent m = new MutableStudent("192311001", "Arun");
        ms.add(m);
        System.out.println("before change: contains = " + ms.contains(m) + ", size = " + ms.size());
        m.regNo = "192311999";
        System.out.println("after change : contains = " + ms.contains(m) + ", size = " + ms.size() + ", remove = " + ms.remove(m));
        ms.add(m);
        System.out.println("add same object again: size = " + ms.size() + "  (the same object is now stored twice)");

        // ---------- Test 3b: TreeSet comparator inconsistent with equals ----------
        header("Test 3b - TreeSet ordered by name only, equals() on regNo");
        System.out.println("Prediction: TreeSet size 1 (compare == 0 means 'duplicate'), HashSet size 2 (different regNo).");
        Comparator<GoodStudent> byName = Comparator.comparing(g -> g.name);
        TreeSet<GoodStudent> ts = new TreeSet<>(byName); Set<GoodStudent> hs = new HashSet<>();
        for (GoodStudent g : List.of(new GoodStudent("192311001", "Arun"), new GoodStudent("192311077", "Arun"))) { ts.add(g); hs.add(g); }
        System.out.println("Observed TreeSet size = " + ts.size() + ", HashSet size = " + hs.size());

        // ---------- Test 4: legal but poor hashCode ----------
        header("Test 4 - hashCode() constant 42 (legal, equals consistent) - cost of collisions");
        System.out.printf("%-8s %16s %16s%n", "n", "constant-hash ms", "regNo-hash ms");
        for (int n : new int[]{2000, 4000, 8000, 16000}) {
            String[] regs = new String[n];
            for (int i = 0; i < n; i++) regs[i] = String.format("1923%06d", i);
            double tc = Bench.mean(Bench.measure(() -> { Set<ConstantHashStudent> s = new HashSet<>(); for (String r : regs) s.add(new ConstantHashStudent(r)); Bench.sink = s.size(); }, 2, 5)) / 1e6;
            double tg = Bench.mean(Bench.measure(() -> { Set<GoodStudent> s = new HashSet<>(); for (String r : regs) s.add(new GoodStudent(r, "x")); Bench.sink = s.size(); }, 2, 5)) / 1e6;
            System.out.printf("%-8d %16.2f %16.2f%n", n, tc, tg);
        }
    }
}
