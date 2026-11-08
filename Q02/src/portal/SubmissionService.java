import java.util.*;

public class SubmissionService {
    private final Storage storage = new Storage();
    private final Map<String, Set<String>> registered = new HashMap<>();
    private final int closingHour = 12;                       // window closes at 12:00
    public SubmissionService() {
        registered.put("CSA09", new HashSet<>(Arrays.asList("S1", "S2", "S3")));
    }
    public void submit(String student, String course, String file, long sizeKb, int hour)
            throws ExamException {
        if (file == null || file.isBlank()) throw new InvalidSubmissionException("file name is blank");
        if (hour >= closingHour) throw new ExamWindowClosedException("Exam window closed at " + closingHour + ":00");
        if (!registered.getOrDefault(course, Set.of()).contains(student))
            throw new NotRegisteredException(student + " is not registered for " + course);
        storage.save(file, sizeKb);                           // may throw SubmissionStorageException
    }
}
