// Alternative concept: no exceptions, only pre-checks + error codes, plus a micro benchmark.
public class ErrorCodeAtm {
    static final int BAD_FORMAT = -1, NEGATIVE = -2, NO_FUNDS = -3;
    static int withdraw(String amountText, int balance) {
        if (amountText == null || !amountText.matches("-?\\d+")) return BAD_FORMAT;   // check 1
        int amount = Integer.parseInt(amountText);
        if (amount <= 0) return NEGATIVE;                                              // check 2
        if (amount > balance) return NO_FUNDS;                                         // check 3
        return balance - amount;
    }
    static int withdrawEx(String amountText, int balance) {
        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) throw new IllegalArgumentException();
            if (amount > balance) throw new IllegalStateException();
            return balance - amount;
        } catch (NumberFormatException e) { return BAD_FORMAT; }
          catch (IllegalArgumentException e) { return NEGATIVE; }
          catch (IllegalStateException e) { return NO_FUNDS; }
    }
    public static void main(String[] args) {
        System.out.println("codes : " + withdraw("500", 1000) + " " + withdraw("abc", 1000)
                + " " + withdraw("-5", 1000) + " " + withdraw("5000", 1000));
        int N = 200_000; long sink = 0;
        for (String[] c : new String[][]{{"valid","500"},{"invalid","abc"}}) {
            for (int w = 0; w < 3; w++)
                for (int i = 0; i < N; i++) sink += withdraw(c[1], 1000) + withdrawEx(c[1], 1000);
            long t0 = System.nanoTime();
            for (int i = 0; i < N; i++) sink += withdraw(c[1], 1000);
            long codes = System.nanoTime() - t0;
            t0 = System.nanoTime();
            for (int i = 0; i < N; i++) sink += withdrawEx(c[1], 1000);
            long ex = System.nanoTime() - t0;
            System.out.printf("%-8s input: error-code %5d ns/op | exception %5d ns/op%n", c[0], codes / N, ex / N);
        }
        if (sink == 42) System.out.println();
    }
}
