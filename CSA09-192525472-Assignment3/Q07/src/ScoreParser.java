import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class ScoreParser {
    // One line: studentId,m1,m2,m3,m4,m5   (marks 0..100)
    static FileOutcome parse(String name, Path p) throws IOException, ScoreFormatException, ScoreValidationException {
        List<String> lines = Files.readAllLines(p);            // NoSuchFileException / IOException propagate
        if (lines.isEmpty() || lines.get(0).isBlank()) throw new ScoreFormatException("empty file");
        String[] f = lines.get(0).trim().split(",");
        if (f.length != 6) throw new ScoreFormatException("expected 6 fields but found " + f.length);
        int sum = 0;
        for (int i = 1; i < 6; i++) {
            int m;
            try { m = Integer.parseInt(f[i].trim()); }
            catch (NumberFormatException e) { throw new ScoreFormatException("non-numeric mark '" + f[i] + "'", e); }
            if (m < 0 || m > 100) throw new ScoreValidationException("mark " + m + " outside 0..100");
            sum += m;
        }
        double avg = sum / 5.0;
        String g = avg >= 90 ? "O" : avg >= 80 ? "A" : avg >= 70 ? "B" : avg >= 60 ? "C" : avg >= 50 ? "D" : "F";
        return FileOutcome.ok(name, f[0], avg, g);
    }
}
