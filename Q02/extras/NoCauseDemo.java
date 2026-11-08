import java.io.IOException;
class ExamException extends Exception {
    ExamException(String message) { super(message); }
    ExamException(String message, Throwable cause) { super(message, cause); }
}
class SubmissionStorageException extends ExamException {
    SubmissionStorageException(String message, Throwable cause) { super(message, cause); }
    SubmissionStorageException(String message) { super(message); }          // cause NOT passed
}
class Storage {
    void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) throw new IOException("Disk quota exceeded");
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + fileName);   // cause dropped
        }
    }
}
public class NoCauseDemo {
    public static void main(String[] args) {
        try {
            new Storage().save("answer02.pdf", 800);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("getCause() = " + e.getCause());
            try { System.out.println("Cause: " + e.getCause().getMessage()); }
            catch (NullPointerException npe) { System.out.println("Cause line -> NullPointerException (getCause() is null)"); }
        }
    }
}
