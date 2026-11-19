import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Q1 design decision: several threads append to one shared booking log. */
public class SharedLogDemo {
    static final int THREADS = 8, APPENDS = 50_000;

    interface Log { void add(String s); int length(); }

    static String run(String label, Log log, PrintWriter csv, int trial) throws Exception {
        Thread[] ts = new Thread[THREADS];
        String error = "none";
        final String[] err = {null};
        long t0 = System.nanoTime();
        for (int i = 0; i < THREADS; i++) {
            ts[i] = new Thread(() -> {
                try { for (int k = 0; k < APPENDS; k++) log.add("x"); }
                catch (Throwable e) { err[0] = e.getClass().getSimpleName(); }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        long ns = System.nanoTime() - t0;
        if (err[0] != null) error = err[0];
        int expected = THREADS * APPENDS, got = log.length();
        csv.printf("%s,%d,%d,%d,%d,%s,%.1f%n", label, trial, expected, got, expected - got, error, ns / 1e6);
        return String.format("%-34s trial %d: length %7d / %d  lost %6d  exception: %-28s %.1f ms", label, trial, got, expected, expected - got, error, ns / 1e6);
    }

    public static void main(String[] args) throws Exception {
        String out = args.length > 0 ? args[0] : "output";
        Files.createDirectories(Path.of(out));
        try (PrintWriter csv = new PrintWriter(out + "/q1_shared_log.csv")) {
            csv.println("structure,trial,expected_length,actual_length,lost_appends,exception,ms");
            for (int trial = 1; trial <= 5; trial++) {
                StringBuilder sb = new StringBuilder();
                System.out.println(run("StringBuilder (unsynchronised)", new Log() {
                    public void add(String s) { sb.append(s); } public int length() { return sb.length(); } }, csv, trial));
            }
            for (int trial = 1; trial <= 5; trial++) {
                StringBuffer sf = new StringBuffer();
                System.out.println(run("StringBuffer (synchronised)", new Log() {
                    public void add(String s) { sf.append(s); } public int length() { return sf.length(); } }, csv, trial));
            }
            for (int trial = 1; trial <= 5; trial++) {
                StringBuilder sb = new StringBuilder();
                System.out.println(run("StringBuilder + synchronized block", new Log() {
                    public void add(String s) { synchronized (sb) { sb.append(s); } } public int length() { return sb.length(); } }, csv, trial));
            }
            for (int trial = 1; trial <= 5; trial++) {
                ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
                System.out.println(run("ConcurrentLinkedQueue<String>", new Log() {
                    public void add(String s) { q.add(s); } public int length() { return q.size(); } }, csv, trial));
            }
        }
    }
}
