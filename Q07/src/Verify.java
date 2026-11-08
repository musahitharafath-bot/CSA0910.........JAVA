import java.nio.file.*;
import java.util.*;

// Runs the full 10,000-file set once and compares with the generator's independent expected counts.
public class Verify {
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("data");
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        Summary s = ResultProcessor.process(dir, ResultProcessor.manifest(dir), threads, 0);
        System.out.print(s.report(8));
        Map<String, String> exp = new HashMap<>();
        for (String l : Files.readAllLines(dir.resolve("expected_counts.txt"))) { String[] kv = l.split("="); exp.put(kv[0], kv[1]); }
        boolean all = true;
        for (Status st : Status.values()) {
            boolean ok = Long.parseLong(exp.getOrDefault(st.name(), "0")) == s.count(st); all &= ok;
            System.out.printf("%s %-10s expected=%s actual=%d%n", ok ? "PASS" : "FAIL", st, exp.getOrDefault(st.name(), "0"), s.count(st));
        }
        boolean avgOk = String.format("%.2f", s.averageTotal()).equals(exp.get("sumOfAverages")); all &= avgOk;
        System.out.printf("%s sumOfAverages expected=%s actual=%.2f%n", avgOk ? "PASS" : "FAIL", exp.get("sumOfAverages"), s.averageTotal());
        System.out.println(all ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED");
    }
}
