// Student B: getCause(), the full chained stack trace, and what is lost without the cause.
public class ChainDemo {
    static void layerSave(boolean keepCause) throws SubmissionStorageException {
        try {
            throw new java.io.IOException("Disk quota exceeded: 800 KB > 500 KB");
        } catch (java.io.IOException e) {
            if (keepCause) throw new SubmissionStorageException("Could not store big.pdf", e);
            throw new SubmissionStorageException("Could not store big.pdf", null);   // cause thrown away
        }
    }
    static void layerService(boolean keepCause) throws ExamException { layerSave(keepCause); }
    public static void main(String[] args) {
        System.out.println("=== WITH cause ===");
        try { layerService(true); }
        catch (ExamException e) {
            System.out.println("getMessage()       : " + e.getMessage());
            System.out.println("getCause()         : " + e.getCause());
            System.out.println("getCause().getMsg(): " + e.getCause().getMessage());
            e.printStackTrace(System.out);
        }
        System.out.println();
        System.out.println("=== WITHOUT cause ===");
        try { layerService(false); }
        catch (ExamException e) {
            System.out.println("getCause()         : " + e.getCause());
            e.printStackTrace(System.out);
            System.out.println("LOST: the IOException type, its message (quota 800 > 500) and the line where it started.");
        }
    }
}
