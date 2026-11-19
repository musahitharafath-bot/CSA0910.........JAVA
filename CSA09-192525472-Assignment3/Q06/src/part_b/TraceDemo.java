// Student A: the counter size after every operation never leaves [0, 5].
public class TraceDemo {
    public static void main(String[] args) throws Exception {
        Buf b = new Buf(5, true, true);
        Thread cook = new Thread(() -> { try { for (int t = 1; t <= 20; t++) { b.put(t); if (t % 3 == 0) Thread.sleep(2); } } catch (InterruptedException e) { } });
        Thread[] st = new Thread[2]; long[] got = new long[2];
        for (int s = 0; s < 2; s++) { final int id = s;
            st[s] = new Thread(() -> { try { for (int k = 0; k < 10; k++) { got[id] += b.take(); Thread.sleep(id == 0 ? 1 : 3); } } catch (InterruptedException e) { } }); }
        cook.start(); for (Thread t : st) t.start(); cook.join(); for (Thread t : st) t.join();
        System.out.println("operations recorded : " + b.trace.size() + " (20 puts + 20 takes)");
        System.out.println("counter size after each operation: " + b.trace);
        System.out.println("minimum = " + b.minSeen + "  maximum = " + b.maxSeen + "   (limits are 0 and 5)");
        StringBuilder sb = new StringBuilder();
        for (int c : b.trace) sb.append(c);
        System.out.println("as a strip: " + sb);
        System.out.println("sum collected = " + (got[0] + got[1]) + " (expected 210)");
    }
}
