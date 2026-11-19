import java.io.IOException;
// Carries the name of the file that caused the failure (used by the logging requirement).
public class StoreFileException extends IOException {
    final String fileName;
    public StoreFileException(String fileName, String msg, Throwable cause) { super(msg, cause); this.fileName = fileName; }
}
