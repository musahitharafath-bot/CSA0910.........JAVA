public class ExamController {
    private final SubmissionService service = new SubmissionService();
    // Final handling point: converts any ExamException into a message the student can understand.
    public String submit(String student, String course, String file, long sizeKb, int hour) {
        try {
            service.submit(student, course, file, sizeKb, hour);
            return "SUCCESS: " + file + " submitted";
        } catch (ExamWindowClosedException e) {
            return "REJECTED: " + e.getMessage();
        } catch (NotRegisteredException e) {
            return "REJECTED: " + e.getMessage();
        } catch (SubmissionStorageException e) {
            return "SORRY: your file was not saved (" + e.getMessage() + "). Please try a smaller file.";
        } catch (ExamException e) {
            return "ERROR: " + e.getMessage();
        } catch (InvalidSubmissionException e) {
            return "BAD REQUEST: " + e.getMessage();
        }
    }
}
