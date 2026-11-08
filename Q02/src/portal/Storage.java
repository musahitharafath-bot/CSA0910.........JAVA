import java.io.IOException;

public class Storage {
    static final long LIMIT_KB = 500;
    public void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            lowLevelWrite(fileName, sizeKb);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + fileName, e);   // chained
        }
    }
    private void lowLevelWrite(String fileName, long sizeKb) throws IOException {
        if (sizeKb > LIMIT_KB) throw new IOException("Disk quota exceeded: " + sizeKb + " KB > " + LIMIT_KB + " KB");
    }
}
