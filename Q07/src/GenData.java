import java.io.*;
import java.nio.file.*;
import java.util.*;

// Generates N score files with deterministic fault injection, plus manifest.txt and expected_counts.txt.
public class GenData {
    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args.length > 0 ? args[0] : "data");
        int n = args.length > 1 ? Integer.parseInt(args[1]) : 10_000;
        Files.createDirectories(dir);
        List<String> manifest = new ArrayList<>();
        Map<Status, Integer> exp = new EnumMap<>(Status.class);
        double expectedAvgSum = 0;
        for (int i = 0; i < n; i++) {
            String name = (i % 2500 == 5 ? "score_poison_" : "score_") + String.format("%05d", i) + ".txt";
            manifest.add(name);
            Path p = dir.resolve(name);
            Random r = new Random(i);
            int[] m = new int[5]; int sum = 0; for (int k = 0; k < 5; k++) { m[k] = r.nextInt(101); sum += m[k]; }
            String id = "STU" + String.format("%05d", i);
            if (i % 2500 == 5)      { Files.writeString(p, id + "," + sum / 5 + ",50,50,50,50\n"); exp.merge(Status.UNEXPECTED, 1, Integer::sum); }
            else if (i % 1000 == 999) { Files.createDirectories(p); exp.merge(Status.IO_ERROR, 1, Integer::sum); }   // a directory instead of a file
            else if (i % 500 == 33) { Files.writeString(p, ""); exp.merge(Status.CORRUPT, 1, Integer::sum); }          // empty file
            else if (i % 200 == 21) { Files.writeString(p, id + ",140," + m[1] + "," + m[2] + "," + m[3] + "," + m[4] + "\n"); exp.merge(Status.INVALID, 1, Integer::sum); }
            else if (i % 100 == 13) { exp.merge(Status.MISSING, 1, Integer::sum); }                                    // listed but never written
            else if (i % 50 == 7)   { Files.writeString(p, id + "," + m[0] + ",x" + m[1] + "," + m[2] + "," + m[3] + "," + m[4] + "\n"); exp.merge(Status.CORRUPT, 1, Integer::sum); }
            else { Files.writeString(p, id + "," + m[0] + "," + m[1] + "," + m[2] + "," + m[3] + "," + m[4] + "\n"); exp.merge(Status.OK, 1, Integer::sum); expectedAvgSum += sum / 5.0; }
        }
        Files.write(dir.resolve("manifest.txt"), manifest);
        StringBuilder sb = new StringBuilder("files=" + n + "\n");
        for (Status s : Status.values()) sb.append(s).append('=').append(exp.getOrDefault(s, 0)).append('\n');
        sb.append(String.format("sumOfAverages=%.2f%n", expectedAvgSum));
        Files.writeString(dir.resolve("expected_counts.txt"), sb.toString());
        System.out.print("generated " + n + " files in " + dir + "\n" + sb);
    }
}
