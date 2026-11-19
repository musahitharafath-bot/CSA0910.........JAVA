// Student B: a stack trace for one UNCAUGHT exception.
public class UncaughtDemo {
    static int parseAmount(String s) { return Integer.parseInt(s); }
    static int withdraw(String s)   { return parseAmount(s) * 2; }
    public static void main(String[] args) {
        System.out.println("Before");
        withdraw("12x");               // nothing catches this
        System.out.println("After (never printed)");
    }
}
