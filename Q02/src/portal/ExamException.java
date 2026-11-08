// Root of the portal hierarchy. CHECKED: every one of these is an expected, recoverable business failure
// that the controller must handle and explain to the student.
public class ExamException extends Exception {
    public ExamException(String message) { super(message); }
    public ExamException(String message, Throwable cause) { super(message, cause); }
}
