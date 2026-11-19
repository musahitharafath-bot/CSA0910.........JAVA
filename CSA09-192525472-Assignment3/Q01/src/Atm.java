import java.io.*;
import java.util.*;

public class Atm {
    // Reads the card-reader file and returns the balance of the account.
    // FileNotFoundException (checked) when the file is missing.
    static int readBalance(String cardFile, String acc) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(cardFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p[0].equals(acc)) return Integer.parseInt(p[1].trim());
            }
        }
        throw new IllegalStateException("Unknown account " + acc);   // unchecked
    }

    // Returns the new balance. Every failure point is marked with FAIL-n.
    static int withdraw(String cardFile, String acc, String amountText)
            throws IOException, InsufficientFundsException {
        int balance = readBalance(cardFile, acc);                    // FAIL-1 missing file, FAIL-2 unknown account
        int amount = Integer.parseInt(amountText.trim());            // FAIL-3 letters typed
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive"); // FAIL-4
        if (amount > balance)                                        // FAIL-5
            throw new InsufficientFundsException("Balance " + balance + " < requested " + amount);
        return balance - amount;
    }

    // Complete handler with multi-catch and finally. Returns a label of the path taken.
    static String run(String cardFile, String acc, String amountText) {
        String path;
        try {
            int left = withdraw(cardFile, acc, amountText);
            path = "OK, balance left " + left;
        } catch (FileNotFoundException e) {                          // narrower IOException first
            path = "catch FileNotFoundException: card reader file missing";
        } catch (IOException e) {
            path = "catch IOException: " + e.getMessage();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { // unrelated types -> legal multi-catch
            path = "catch multi(NumberFormat|ArrayIndex): invalid number or malformed record -> " + e.getMessage();
        } catch (IllegalArgumentException e) {                       // parent of NumberFormatException, so it comes AFTER it
            path = "catch IllegalArgumentException: " + e.getMessage();
        } catch (InsufficientFundsException e) {
            path = "catch InsufficientFundsException: " + e.getMessage();
        } catch (RuntimeException e) {                               // last safety net for unchecked
            path = "catch RuntimeException: " + e.getMessage();
        } finally {
            System.out.println("   [finally] card returned, session closed");
        }
        return path;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Account: "); String acc = sc.nextLine();
        System.out.print("Amount : "); String amt = sc.nextLine();
        System.out.println(run("input/cardreader.txt", acc, amt));
    }
}
