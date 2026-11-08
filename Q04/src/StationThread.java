import java.util.*;

// Way 1: extend Thread.
public class StationThread extends Thread {
    static final List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());
    final int scripts; volatile int scanned = 0; final boolean quiet;
    public StationThread(String name, int scripts, boolean quiet) { super(name); this.scripts = scripts; this.quiet = quiet; }
    @Override public void run() {
        try {
            for (int i = 0; i < scripts; i += 5) { Thread.sleep(8); scanned += Math.min(5, scripts - i); }
        } catch (InterruptedException e) { return; }
        finishOrder.add(getName());
        if (!quiet) System.out.println(getName() + " finished, scanned " + scanned);
    }
}
