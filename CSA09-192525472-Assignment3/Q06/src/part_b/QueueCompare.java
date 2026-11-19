import java.util.concurrent.*;

// Alternative: ArrayBlockingQueue(5) vs wait/notifyAll TokenCounter. 1 cook, 2 students, 200,000 tokens.
public class QueueCompare {
    static final int N = 200_000;
    interface Pc { void put(int v) throws Exception; int take() throws Exception; }
    static double run(Pc pc) throws Exception {
        long[] got = new long[2];
        Thread cook = new Thread(() -> { try { for (int t = 1; t <= N; t++) pc.put(t); } catch (Exception e) { } });
        Thread[] st = new Thread[2];
        for (int s = 0; s < 2; s++) { final int id = s;
            st[s] = new Thread(() -> { try { for (int k = 0; k < N / 2; k++) got[id] += pc.take(); } catch (Exception e) { } }); }
        long t0 = System.nanoTime();
        cook.start(); for (Thread t : st) t.start(); cook.join(); for (Thread t : st) t.join();
        long ns = System.nanoTime() - t0;
        long expect = (long) N * (N + 1) / 2;
        if (got[0] + got[1] != expect) System.out.println("WRONG SUM " + (got[0] + got[1]));
        return ns / (double) N;
    }
    public static void main(String[] args) throws Exception {
        TokenCounter tc = new TokenCounter();
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(5);
        Pc a = new Pc() { public void put(int v) throws Exception { tc.put(v); } public int take() throws Exception { return tc.take(); } };
        Pc b = new Pc() { public void put(int v) throws Exception { q.put(v); } public int take() throws Exception { return q.take(); } };
        for (int w = 0; w < 2; w++) { run(a); run(b); }                      // warm-up
        double ta = 0, tb = 0; for (int i = 0; i < 5; i++) { ta += run(a); tb += run(b); }
        System.out.printf("wait()/notifyAll()   : %.0f ns per token (put+take), sum verified%n", ta / 5);
        System.out.printf("ArrayBlockingQueue(5): %.0f ns per token (put+take), sum verified%n", tb / 5);
        System.out.println("space: buffer int[5] = 20 bytes + 3 ints  vs  Object[5] + 2 ReentrantLock conditions + boxed Integers");
    }
}
