// Broader catch BEFORE narrower: javac rejects this file.
public class BadCatchOrder {
    public static void main(String[] a) {
        try { Integer.parseInt("x"); }
        catch (RuntimeException e) { System.out.println("broad"); }
        catch (NumberFormatException e) { System.out.println("narrow"); }  // unreachable
    }
}
