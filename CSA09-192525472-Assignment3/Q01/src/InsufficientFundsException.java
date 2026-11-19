// Checked: the caller must decide what to do when the balance is too low.
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) { super(message); }
}
