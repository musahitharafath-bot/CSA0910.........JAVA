import java.util.concurrent.atomic.AtomicInteger;

// Student A: while vs if around wait(), and notify() vs notifyAll().
public class WhileIfExperiment {
    // 1 cook (20 tokens), 2 students (10 each) like the assignment. Returns {sumOK(1/0), stuck(1/0), negativeCount(1/0)}
    static int[] trial(boolean useWhile) throws Exception {
        Buf b = new Buf(5, useWhile, true);
        long[] got = new long[2]; AtomicInteger errors = new AtomicInteger();
        Thread cook = new Thread(() -> { try { for (int t = 1; t <= 20; t++) b.put(t); } catch (InterruptedException e) { } });
        Thread[] st = new Thread[2];
        for (int s = 0; s < 2; s++) { final int id = s;
            st[s] = new Thread(() -> { try { for (int k = 0; k < 10; k++) got[id] += b.take(); } catch (Throwable e) { errors.incrementAndGet(); } }); }
        cook.setDaemon(true); for (Thread t : st) t.setDaemon(true);
        cook.start(); for (Thread t : st) t.start();
        cook.join(300); for (Thread t : st) t.join(300);
        boolean stuck = cook.isAlive() || st[0].isAlive() || st[1].isAlive();
        return new int[]{ got[0] + got[1] == 210 && !stuck ? 1 : 0, stuck ? 1 : 0, b.minSeen < 0 ? 1 : 0 };
    }
    // capacity 1, 2 producers + 2 consumers, 20 items each. true if all finish.
    static boolean multi(boolean notifyAllFlag) throws Exception {
        Buf b = new Buf(1, true, notifyAllFlag);
        Thread[] t = new Thread[4];
        for (int i = 0; i < 2; i++) t[i] = new Thread(() -> { try { for (int k = 0; k < 20; k++) b.put(k); } catch (InterruptedException e) { } });
        for (int i = 2; i < 4; i++) t[i] = new Thread(() -> { try { for (int k = 0; k < 20; k++) b.take(); } catch (InterruptedException e) { } });
        for (Thread x : t) { x.setDaemon(true); x.start(); }
        long end = System.currentTimeMillis() + 300;
        for (Thread x : t) x.join(Math.max(1, end - System.currentTimeMillis()));
        for (Thread x : t) if (x.isAlive()) return false;
        return true;
    }
    public static void main(String[] args) throws Exception {
        int N = 300;
        for (boolean w : new boolean[]{true, false}) {
            int ok = 0, stuck = 0, neg = 0;
            for (int i = 0; i < N; i++) { int[] r = trial(w); ok += r[0]; stuck += r[1]; neg += r[2]; }
            System.out.printf("%-12s %d trials: correct sum=%3d  wrong/stuck=%3d  (counter went negative in %d trials, stuck %d)%n",
                    w ? "while (loop)" : "if", N, ok, N - ok, neg, stuck);
        }
        System.out.println();
        for (boolean na : new boolean[]{true, false}) {
            int done = 0, M = 40;
            for (int i = 0; i < M; i++) if (multi(na)) done++;
            System.out.printf("%-10s capacity 1, 2 producers + 2 consumers: %d of %d trials completed, %d deadlocked%n",
                    na ? "notifyAll()" : "notify()", done, M, M - done);
        }
    }
}
