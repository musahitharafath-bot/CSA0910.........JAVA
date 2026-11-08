// Student A: prints the propagation path of the storage exception, frame by frame.
public class PropagationDemo {
    public static void main(String[] args) {
        try { new SubmissionService().submit("S2", "CSA09", "big.pdf", 800, 10); }
        catch (ExamException e) {
            System.out.println("Caught by controller. Propagation path (innermost first):");
            for (StackTraceElement f : e.getStackTrace())
                System.out.println("   " + f.getClassName() + "." + f.getMethodName() + "()  line " + f.getLineNumber());
            System.out.println("Root cause: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
        }
    }
}
