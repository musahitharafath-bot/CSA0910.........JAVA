import java.io.*;
import java.util.logging.*;

public class ReportGenerator {
    static final Logger LOG = Logger.getLogger("report");
    static {
        LOG.setUseParentHandlers(false);
        try {
            FileHandler fh = new FileHandler("output/report.log", true);
            fh.setFormatter(new SimpleFormatter() {
                @Override public String format(LogRecord r) { return r.getLevel() + " " + r.getMessage() + "\n"; }
            });
            LOG.addHandler(fh);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    // Sum of qty*price for one store file; a malformed line raises StoreFileException naming the file.
    static double storeTotal(BufferedReader r, String file) throws IOException {
        double total = 0; String line; int n = 0;
        while ((line = r.readLine()) != null) {
            n++;
            String[] p = line.split(",");
            try { total += Integer.parseInt(p[1].trim()) * Double.parseDouble(p[2].trim()); }
            catch (RuntimeException e) {
                LOG.severe("file=" + file + " line=" + n + " text='" + line + "' cause=" + e);
                throw new StoreFileException(file, "Corrupt record in " + file + " at line " + n, e);
            }
        }
        return total;
    }

    // V0: manual close() but NOT in finally -> leaks when an exception occurs.
    static void v0Naive(String[] files, String out) throws IOException {
        TrackedWriter w = new TrackedWriter(out);
        double grand = 0;
        for (String f : files) {
            TrackedReader r = new TrackedReader(f);
            double t = storeTotal(r, f);
            r.close();                                   // skipped if storeTotal throws
            w.write(String.format("%s total=%.2f%n", f, t)); grand += t;
        }
        w.write(String.format("GRAND TOTAL=%.2f%n", grand));
        w.close();                                       // skipped on failure -> buffered data lost
    }

    // V1: try-catch-finally with manual close(), each close guarded separately.
    static void v1Finally(String[] files, String out) throws IOException {
        TrackedWriter w = null;
        try {
            w = new TrackedWriter(out);
            double grand = 0;
            for (String f : files) {
                TrackedReader r = null;
                try {
                    r = new TrackedReader(f);
                    double t = storeTotal(r, f);
                    w.write(String.format("%s total=%.2f%n", f, t)); grand += t;
                } finally {
                    if (r != null) { try { r.close(); } catch (IOException ignore) { } }
                }
            }
            w.write(String.format("GRAND TOTAL=%.2f%n", grand));
        } finally {
            if (w != null) { try { w.close(); } catch (IOException ignore) { } }
        }
    }

    // V2: try-with-resources.
    static void v2Twr(String[] files, String out) throws IOException {
        try (TrackedWriter w = new TrackedWriter(out)) {
            double grand = 0;
            for (String f : files) {
                try (TrackedReader r = new TrackedReader(f)) {
                    double t = storeTotal(r, f);
                    w.write(String.format("%s total=%.2f%n", f, t)); grand += t;
                }
            }
            w.write(String.format("GRAND TOTAL=%.2f%n", grand));
        }
    }

    // V3: no closing at all.
    static void v3NoClose(String[] files, String out) throws IOException {
        TrackedWriter w = new TrackedWriter(out);
        double grand = 0;
        for (String f : files) {
            TrackedReader r = new TrackedReader(f);
            double t = storeTotal(r, f);
            w.write(String.format("%s total=%.2f%n", f, t)); grand += t;
        }
        w.write(String.format("GRAND TOTAL=%.2f%n", grand));
    }

    interface Version { void run(String[] f, String out) throws IOException; }

    static void attempt(String label, Version v, String[] files, String out) {
        TrackedReader.resetCounters();
        String result = "completed";
        try { v.run(files, out); }
        catch (StoreFileException e) { result = "FAILED on " + e.fileName; }
        catch (IOException e) { result = "FAILED " + e; }
        System.out.printf("%-22s %-28s open handles left=%d %s  report bytes=%d%n",
                label, result, TrackedReader.open, TrackedReader.openNames, new File(out).length());
    }

    public static void main(String[] args) throws Exception {
        String[] good = {"input/store1.csv", "input/store2.csv", "input/store3_fixed.csv", "input/store4.csv"};
        String[] bad  = {"input/store1.csv", "input/store2.csv", "input/store3_corrupt.csv", "input/store4.csv"};
        System.out.println("--- good input (4 files) ---");
        attempt("V0 naive",            ReportGenerator::v0Naive,   good, "output/report_v0_good.txt");
        attempt("V1 finally",          ReportGenerator::v1Finally, good, "output/report_v1_good.txt");
        attempt("V2 try-with-res",     ReportGenerator::v2Twr,     good, "output/report_v2_good.txt");
        attempt("V3 no close",         ReportGenerator::v3NoClose, good, "output/report_v3_good.txt");
        boolean same = java.nio.file.Files.readString(java.nio.file.Path.of("output/report_v1_good.txt"))
                .equals(java.nio.file.Files.readString(java.nio.file.Path.of("output/report_v2_good.txt")));
        System.out.println("V1 report identical to V2 report: " + same);
        System.out.print(java.nio.file.Files.readString(java.nio.file.Path.of("output/report_v2_good.txt")));
        System.out.println("--- failing input (corrupt record in store3) ---");
        attempt("V0 naive",            ReportGenerator::v0Naive,   bad, "output/report_v0_bad.txt");
        attempt("V1 finally",          ReportGenerator::v1Finally, bad, "output/report_v1_bad.txt");
        attempt("V2 try-with-res",     ReportGenerator::v2Twr,     bad, "output/report_v2_bad.txt");
        attempt("V3 no close",         ReportGenerator::v3NoClose, bad, "output/report_v3_bad.txt");
        System.out.println("Partial report written by V2:");
        System.out.print(java.nio.file.Files.readString(java.nio.file.Path.of("output/report_v2_bad.txt")));
    }
}
