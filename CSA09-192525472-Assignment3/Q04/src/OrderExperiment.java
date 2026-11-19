import java.util.*;

// Student B: 10 runs of the 4 stations, finishing order recorded; then with priorities.
public class OrderExperiment {
    static String runOnce(boolean priorities) throws Exception {
        StationThread.finishOrder.clear();
        Thread[] t = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t[i] = new StationThread("S" + (i + 1), 25, true);       // identical workload on purpose
            if (priorities) t[i].setPriority(i == 3 ? Thread.MAX_PRIORITY : i == 0 ? Thread.MIN_PRIORITY : Thread.NORM_PRIORITY);
        }
        for (Thread x : t) x.start();
        for (Thread x : t) x.join();
        return String.join(" ", StationThread.finishOrder);
    }
    static void experiment(String title, boolean priorities) throws Exception {
        System.out.println("=== " + title + " ===");
        Map<String, Integer> distinct = new LinkedHashMap<>();
        Map<String, Integer> first = new TreeMap<>();
        for (int r = 1; r <= 12; r++) {
            String o = runOnce(priorities);
            System.out.printf("run %2d finish order: %s%n", r, o);
            distinct.merge(o, 1, Integer::sum); first.merge(o.substring(0, 2), 1, Integer::sum);
        }
        System.out.println("distinct orders seen : " + distinct.size() + " of 12 runs");
        System.out.println("who finished first   : " + first);
    }
    public static void main(String[] args) throws Exception {
        experiment("equal priorities", false);
        System.out.println();
        experiment("S1=MIN_PRIORITY(1), S4=MAX_PRIORITY(10), others NORM(5)", true);
    }
}
