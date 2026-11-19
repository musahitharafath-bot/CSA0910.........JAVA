// UNCHECKED: a programming/argument error (null or blank file name). The student cannot "recover" from it,
// so callers are not forced to catch it.
public class InvalidSubmissionException extends RuntimeException {
    public InvalidSubmissionException(String message) { super(message); }
}
