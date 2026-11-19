import java.nio.file.*;
import java.util.*;

// Student B: hand-checked set of 10 files. Expected values were worked out by hand BEFORE running.
public class SmallCheck {
    static int pass = 0, fail = 0;
    static void check(String what, Object exp, Object act) {
        boolean ok = exp.equals(act); if (ok) pass++; else fail++;
        System.out.printf("%s %-30s expected=%s actual=%s%n", ok ? "PASS" : "FAIL", what, exp, act);
    }
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("input/small");
        Summary s = ResultProcessor.process(dir, ResultProcessor.manifest(dir), 4, 0);
        System.out.print(s.report(10));
        check("OK count", 5L, s.count(Status.OK));
        check("CORRUPT count (letters+empty)", 2L, s.count(Status.CORRUPT));
        check("INVALID count", 1L, s.count(Status.INVALID));
        check("MISSING count", 1L, s.count(Status.MISSING));
        check("UNEXPECTED count (poison)", 1L, s.count(Status.UNEXPECTED));
        check("total accounted", 10L, s.count(Status.OK) + s.count(Status.CORRUPT) + s.count(Status.INVALID) + s.count(Status.MISSING) + s.count(Status.UNEXPECTED) + s.count(Status.IO_ERROR));
        check("sum of averages (80+50+100+0+75)", "305.00", String.format("%.2f", s.averageTotal()));
        check("ok1 grade", "A", s.results.get("ok1.txt").grade());
        check("ok4 grade", "F", s.results.get("ok4.txt").grade());
        check("summary complete", true, s.complete());
        System.out.println(pass + " passed, " + fail + " failed");
    }
}
