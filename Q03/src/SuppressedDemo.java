// Student B: closing order and suppressed exceptions in try-with-resources.
public class SuppressedDemo {
    static class Res implements AutoCloseable {
        final String name; final boolean failOnClose;
        Res(String n, boolean f) { name = n; failOnClose = f; System.out.println("open  " + n); }
        @Override public void close() throws Exception {
            System.out.println("close " + name);
            if (failOnClose) throw new Exception("close() failed for " + name);
        }
    }
    public static void main(String[] args) {
        try (Res reader = new Res("reader", true); Res writer = new Res("writer", true)) {
            System.out.println("body runs");
            throw new IllegalStateException("body failed");
        } catch (Exception e) {
            System.out.println("PRIMARY   : " + e);
            for (Throwable s : e.getSuppressed()) System.out.println("SUPPRESSED: " + s);
        }
        System.out.println("-- body fine, close fails: it becomes the primary exception --");
        try (Res only = new Res("only", true)) { System.out.println("body ok"); }
        catch (Exception e) { System.out.println("PRIMARY   : " + e + "  suppressed=" + e.getSuppressed().length); }
    }
}
