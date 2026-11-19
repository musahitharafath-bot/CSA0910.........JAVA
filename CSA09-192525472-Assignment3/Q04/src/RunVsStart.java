// Student A: evidence that run() does NOT create a new thread.
public class RunVsStart {
    public static void main(String[] args) throws Exception {
        Runnable r = () -> System.out.println("   body executes in thread: " + Thread.currentThread().getName()
                + "  (id " + Thread.currentThread().threadId() + ")");
        System.out.println("main thread is: " + Thread.currentThread().getName() + "  (id " + Thread.currentThread().threadId() + ")");
        Thread t1 = new Thread(r, "worker-A");
        System.out.println("t1.run():");   t1.run();                  // ordinary method call on the main thread
        System.out.println("   t1 state after run(): " + t1.getState());
        Thread t2 = new Thread(r, "worker-B");
        System.out.println("t2.start():"); t2.start(); t2.join();     // JVM creates a new thread
        System.out.println("   t2 state after join(): " + t2.getState());
        try { t2.start(); } catch (IllegalThreadStateException e) { System.out.println("t2.start() again -> " + e); }
    }
}
