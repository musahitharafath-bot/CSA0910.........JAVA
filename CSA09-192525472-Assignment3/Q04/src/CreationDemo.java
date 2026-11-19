// Shared work: both creation styles with start(), sleep(), join(), isAlive().
public class CreationDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("=== extends Thread ===");
        StationThread[] st = new StationThread[4];
        for (int i = 0; i < 4; i++) { st[i] = new StationThread("Station-" + (i + 1), 25 + 5 * i, false); st[i].start(); }
        Thread.sleep(20);
        System.out.println("alive after 20 ms : " + st[0].isAlive() + " " + st[1].isAlive() + " " + st[2].isAlive() + " " + st[3].isAlive());
        for (StationThread t : st) t.join();
        System.out.println("alive after join  : " + st[0].isAlive() + " " + st[1].isAlive() + " " + st[2].isAlive() + " " + st[3].isAlive());
        int total = 0; for (StationThread t : st) total += t.scanned;
        System.out.println("TOTAL scanned (extends Thread) = " + total);

        System.out.println("\n=== implements Runnable ===");
        StationTask[] tk = new StationTask[4]; Thread[] th = new Thread[4];
        for (int i = 0; i < 4; i++) { tk[i] = new StationTask("Station-" + (i + 1), 25 + 5 * i, false); th[i] = new Thread(tk[i]); th[i].start(); }
        for (Thread t : th) t.join();
        total = 0; for (StationTask t : tk) total += t.scanned;
        System.out.println("TOTAL scanned (Runnable)       = " + total + "   (printed only after every join() returned)");
    }
}
