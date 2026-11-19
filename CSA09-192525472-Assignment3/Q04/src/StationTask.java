// Way 2: implement Runnable (the task is separate from the thread that runs it).
public class StationTask implements Runnable {
    final String name; final int scripts; volatile int scanned = 0; final boolean quiet;
    public StationTask(String name, int scripts, boolean quiet) { this.name = name; this.scripts = scripts; this.quiet = quiet; }
    @Override public void run() {
        try {
            for (int i = 0; i < scripts; i += 5) { Thread.sleep(8); scanned += Math.min(5, scripts - i); }
        } catch (InterruptedException e) { return; }
        StationThread.finishOrder.add(name);
        if (!quiet) System.out.println(name + " finished, scanned " + scanned);
    }
}
