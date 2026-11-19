public class SubmissionStorageException extends ExamException {     // checked, always carries the low-level cause
    public SubmissionStorageException(String message, Throwable cause) { super(message, cause); }
}
