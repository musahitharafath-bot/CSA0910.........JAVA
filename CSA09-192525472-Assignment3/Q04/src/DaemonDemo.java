// Student B: user thread vs daemon thread when main() ends.  usage: java DaemonDemo user|daemon
public class DaemonDemo {
    public static void main(String[] args) throws Exception {
        boolean daemon = args.length > 0 && args[0].equals("daemon");
        long start = System.currentTimeMillis();
        Thread slow = new Thread(() -> {
            try { Thread.sleep(1500); System.out.println("  Station-4 finished its batch after " + (System.currentTimeMillis() - start) + " ms"); }
            catch (InterruptedException e) { }
        }, "Station-4");
        slow.setDaemon(daemon);
        slow.start();
        Thread.sleep(200);
        System.out.println("mode=" + (daemon ? "DAEMON" : "USER") + ": main() ends at " + (System.currentTimeMillis() - start) + " ms");
    }
}
