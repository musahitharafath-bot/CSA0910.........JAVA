import java.lang.management.ManagementFactory;

// Cost of capturing a stack trace (time + allocated bytes) at different call depths,
// with and without chaining, and with the stack trace switched off.
public class ExceptionCost {
    static class Quiet extends Exception {       // writableStackTrace = false -> no fillInStackTrace cost
        Quiet(String m, Throwable c) { super(m, c, false, false); }
    }
    static Throwable make(int depth, int mode) {
        if (depth > 0) return make(depth - 1, mode);
        if (mode == 0) return new Exception("x");
        if (mode == 1) return new Exception("outer", new Exception("inner"));
        return new Quiet("x", null);
    }
    static long allocated() {
        return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean())
                .getThreadAllocatedBytes(Thread.currentThread().getId());
    }
    public static void main(String[] args) {
        int N = 50_000; long sink = 0;
        String[] names = {"single exception", "chained (outer+inner)", "no stack trace"};
        System.out.printf("%-8s %-24s %10s %12s%n", "depth", "kind", "ns/exc", "bytes/exc");
        for (int depth : new int[]{5, 50, 500}) {
            for (int mode = 0; mode < 3; mode++) {
                for (int i = 0; i < 20_000; i++) sink += make(depth, mode).hashCode();   // warm-up
                long b0 = allocated(), t0 = System.nanoTime();
                for (int i = 0; i < N; i++) sink += make(depth, mode).getStackTrace().length;
                long t = System.nanoTime() - t0, b = allocated() - b0;
                System.out.printf("%-8d %-24s %10d %12d%n", depth, names[mode], t / N, b / N);
            }
        }
        if (sink == 1) System.out.println();
    }
}
